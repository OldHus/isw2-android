package com.svape.unal.ingesoftii.presentation.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.svape.unal.ingesoftii.domain.model.UserRole
import com.svape.unal.ingesoftii.domain.usecase.SignOutUseCase
import com.svape.unal.ingesoftii.domain.usecase.UnsubscribeFromCourseNotificationsUseCase
import com.svape.unal.ingesoftii.presentation.session.CurrentSessionHolder
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val USERS_COLLECTION = "usuarios"
private const val NAME_FIELD = "nombre"
private const val PHOTO_URL_FIELD = "fotoUrl"

data class UserProfile(
    val displayName: String,
    val email: String,
    val photoUrl: String?,
) {
    val firstName: String get() = displayName.trim().split(" ").firstOrNull().orEmpty()
}

class AccountViewModel(
    private val signOutUseCase: SignOutUseCase,
    private val unsubscribeFromCourseNotificationsUseCase: UnsubscribeFromCourseNotificationsUseCase,
    private val currentSessionHolder: CurrentSessionHolder
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _profile = MutableStateFlow(profileFromAuth())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    private val _isSigningOut = MutableStateFlow(false)
    val isSigningOut: StateFlow<Boolean> = _isSigningOut.asStateFlow()

    init {
        loadProfileFromFirestore()
    }

    fun signOut(onComplete: () -> Unit) {
        if (_isSigningOut.value) return
        _isSigningOut.value = true
        val session = currentSessionHolder.current.value
        viewModelScope.launch {
            if (session != null) unsubscribeFromNotifications(session.courseId)
            signOutUseCase()
            currentSessionHolder.clear()
            onComplete()
        }
    }

    private fun unsubscribeFromNotifications(courseId: String) {
        listOf(UserRole.TEACHER, UserRole.STUDENT).forEach { role ->
            viewModelScope.launch {
                withContext(NonCancellable) {
                    unsubscribeFromCourseNotificationsUseCase(courseId, role)
                }
            }
        }
    }

    private fun loadProfileFromFirestore() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            runCatching {
                firestore.collection(USERS_COLLECTION).document(uid).get().await()
            }.onSuccess { doc ->
                if (!doc.exists()) return@onSuccess
                val current = _profile.value
                val name = doc.getString(NAME_FIELD)?.takeIf { it.isNotBlank() }?.toTitleCase()
                val photo = doc.getString(PHOTO_URL_FIELD)?.takeIf { it.isNotBlank() }
                _profile.value = current.copy(
                    displayName = name ?: current.displayName,
                    photoUrl = photo,
                )
            }
        }
    }

    private fun profileFromAuth(): UserProfile {
        val user = auth.currentUser
        val email = user?.email.orEmpty()
        val rawName = user?.displayName?.takeIf { it.isNotBlank() }
            ?: email.substringBefore("@").replace('.', ' ')
        return UserProfile(
            displayName = rawName.toTitleCase().ifBlank { "Usuario" },
            email = email,
            photoUrl = user?.photoUrl?.toString(),
        )
    }

    private fun String.toTitleCase(): String =
        trim().split(Regex("\\s+")).joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.titlecase() }
        }
}