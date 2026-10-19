package com.svape.unal.ingesoftii.data.repository

import android.content.Context
import android.util.Base64
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.svape.unal.ingesoftii.core.logging.SafeLog
import com.svape.unal.ingesoftii.core.logging.SanitizedException
import com.svape.unal.ingesoftii.domain.model.AuthError
import com.svape.unal.ingesoftii.domain.model.AuthResult
import com.svape.unal.ingesoftii.domain.model.UserRole
import com.svape.unal.ingesoftii.domain.repository.AuthRepository
import com.svape.unal.ingesoftii.domain.service.CrashReporter
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.security.MessageDigest
import java.security.SecureRandom

private const val TAG = "AuthRepository"
private const val ALLOWED_EMAIL_DOMAIN = "@unal.edu.co"
private const val HOSTED_DOMAIN = "unal.edu.co"
private const val USERS_COLLECTION = "usuarios"
private const val ROLE_FIELD = "rol"
private const val COURSE_ID_FIELD = "cursoId"
private const val ERROR_USER_DISABLED = "ERROR_USER_DISABLED"

private enum class AuthStep { CREDENTIAL_MANAGER, FIREBASE_AUTH, DOMAIN_CHECK, FIRESTORE }

private class GoogleSignInCancelledException(message: String) : Exception(message)

class AuthRepositoryImpl(
    private val context: Context,
    private val webClientId: String,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val crashReporter: CrashReporter
) : AuthRepository {

    private val crashlytics: FirebaseCrashlytics by lazy { FirebaseCrashlytics.getInstance() }

    override suspend fun signInWithGoogle(): Result<AuthResult> {
        var step = AuthStep.CREDENTIAL_MANAGER
        return try {
            val idToken = getGoogleIdToken()

            step = AuthStep.FIREBASE_AUTH
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            val user = authResult.user
                ?: return Result.failure(AuthError.Unknown("FirebaseAuth: usuario nulo"))

            step = AuthStep.DOMAIN_CHECK
            val email = user.email.orEmpty()
            if (!isInstitutionalEmail(email)) {
                crashlytics.log("Sign-in rejected: non-institutional domain")
                auth.signOut()
                return Result.failure(AuthError.DomainNotAllowed)
            }

            step = AuthStep.FIRESTORE
            val userDoc = firestore.collection(USERS_COLLECTION).document(user.uid).get().await()

            crashlytics.setUserId(user.uid)
            crashlytics.log("Google sign-in success")

            if (userDoc.exists()) {
                val role = userDoc.getString(ROLE_FIELD) ?: UserRole.STUDENT
                val courseId = userDoc.getString(COURSE_ID_FIELD) ?: ""
                Result.success(AuthResult.ExistingUser(role, courseId))
            } else {
                Result.success(AuthResult.NewUser(user.uid))
            }
        } catch (e: Exception) {
            val authError = mapGoogleError(e)
            reportGoogleFailure(step, e, authError)
            Result.failure(authError)
        }
    }

    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Result<AuthResult> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user
                ?: return Result.failure(AuthError.Unknown("FirebaseAuth: usuario nulo"))

            val userDoc = firestore.collection(USERS_COLLECTION).document(user.uid).get().await()

            crashlytics.setUserId(user.uid)

            if (userDoc.exists()) {
                val role = userDoc.getString(ROLE_FIELD) ?: UserRole.STUDENT
                val courseId = userDoc.getString(COURSE_ID_FIELD) ?: ""
                Result.success(AuthResult.ExistingUser(role, courseId))
            } else {
                Result.success(AuthResult.NewUser(user.uid))
            }
        } catch (e: Exception) {
            val authError = mapEmailError(e)
            if (authError !is AuthError.InvalidCredentials) {
                SafeLog.e(TAG, "Email sign-in failed (${authError.code})", e)
                crashlytics.setCustomKey("auth_method", "email")
                crashlytics.setCustomKey("auth_error", authError.code)
                crashReporter.recordException(SanitizedException(e))
            }
            Result.failure(authError)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
        crashlytics.setUserId("")
        try {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        } catch (e: ClearCredentialException) {
            SafeLog.e(TAG, "Could not clear credential state", e)
        }
    }

    private fun isInstitutionalEmail(email: String): Boolean {
        return email.endsWith(ALLOWED_EMAIL_DOMAIN, ignoreCase = true)
    }


    private fun mapGoogleError(e: Throwable): AuthError = when (e) {
        is GetCredentialCancellationException -> AuthError.CancelledByUser
        is NoCredentialException -> AuthError.NoGoogleAccountAvailable
        is GetCredentialInterruptedException -> AuthError.Interrupted
        is GetCredentialProviderConfigurationException,
        is GetCredentialUnsupportedException -> AuthError.CredentialServiceUnavailable

        is GoogleIdTokenParsingException -> AuthError.InvalidGoogleToken
        is GetCredentialException -> AuthError.Unknown(SafeLog.describe(e))
        is FirebaseAuthUserCollisionException -> AuthError.AccountExistsWithOtherMethod
        is FirebaseAuthInvalidUserException ->
            if (e.errorCode == ERROR_USER_DISABLED) AuthError.AccountDisabled
            else AuthError.Unknown(SafeLog.describe(e))

        is FirebaseAuthInvalidCredentialsException -> AuthError.InvalidGoogleToken
        else -> mapCommonError(e)
    }

    private fun mapEmailError(e: Throwable): AuthError = when (e) {
        is FirebaseAuthInvalidUserException ->
            if (e.errorCode == ERROR_USER_DISABLED) AuthError.AccountDisabled
            else AuthError.InvalidCredentials

        is FirebaseAuthInvalidCredentialsException -> AuthError.InvalidCredentials
        else -> mapCommonError(e)
    }

    private fun mapCommonError(e: Throwable): AuthError = when (e) {
        is FirebaseNetworkException, is IOException -> AuthError.NoInternet
        is FirebaseTooManyRequestsException -> AuthError.TooManyRequests
        is FirebaseFirestoreException ->
            if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) AuthError.NoInternet
            else AuthError.ProfileLoadFailed

        else -> AuthError.Unknown(SafeLog.describe(e))
    }

    private fun reportGoogleFailure(step: AuthStep, error: Throwable, authError: AuthError) {
        SafeLog.e(TAG, "Google sign-in failed at $step (${authError.code})", error)
        crashlytics.setCustomKey("auth_method", "google")
        crashlytics.setCustomKey("auth_step", step.name)
        crashlytics.setCustomKey("auth_error", authError.code)
        crashlytics.setCustomKey("auth_exception", error.javaClass.simpleName)
        crashlytics.setCustomKey("auth_message", SafeLog.sanitize(error.message))
        crashlytics.setCustomKey(
            "credential_error_type",
            SafeLog.sanitize((error as? GetCredentialException)?.type ?: "n/a", 100),
        )
        crashlytics.log("Google sign-in failed at $step: ${authError.code}")

        val reported = if (error is GetCredentialCancellationException) {
            GoogleSignInCancelledException("Google sign-in cancelled: ${SafeLog.sanitize(error.message)}")
                .also { it.stackTrace = error.stackTrace }
        } else {
            error
        }
        crashReporter.recordException(SanitizedException(reported))
    }

    private suspend fun getGoogleIdToken(): String {
        val credentialManager = CredentialManager.create(context)
        val rawNonce = generateNonce()
        val hashedNonce = hashNonce(rawNonce)

        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(serverClientId = webClientId)
            .setNonce(hashedNonce)
            .setHostedDomainFilter(HOSTED_DOMAIN)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        val result = credentialManager.getCredential(context, request)
        val credential = result.credential

        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return GoogleIdTokenCredential.createFrom(credential.data).idToken
        }
        throw IllegalStateException("Unexpected credential type: ${credential.type}")
    }

    override suspend fun getCurrentSession(): AuthResult? {
        val user = auth.currentUser ?: return null

        return try {
            val userDoc = firestore.collection(USERS_COLLECTION).document(user.uid).get().await()
            if (userDoc.exists()) {
                val role = userDoc.getString(ROLE_FIELD) ?: UserRole.STUDENT
                val courseId = userDoc.getString(COURSE_ID_FIELD) ?: ""
                AuthResult.ExistingUser(role, courseId)
            } else {
                AuthResult.NewUser(user.uid)
            }
        } catch (e: Exception) {
            SafeLog.e(TAG, "Could not restore session", e)
            crashReporter.recordException(SanitizedException(e))
            null
        }
    }

    private fun generateNonce(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    private fun hashNonce(nonce: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(nonce.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}