package com.svape.unal.ingesoftii.presentation.auth

import com.svape.unal.ingesoftii.domain.model.AuthError

fun AuthError.toUserMessage(): String = when (this) {
    AuthError.CancelledByUser ->
        "No se completó el inicio de sesión. Si elegiste tu cuenta y volviste a esta pantalla, " +
                "revisa en Ajustes > Cuentas que la sincronización de tu cuenta @unal.edu.co esté activa " +
                "y vuelve a intentarlo."

    AuthError.NoGoogleAccountAvailable ->
        "No hay una cuenta @unal.edu.co disponible en este teléfono. Agrégala en Ajustes > Cuentas " +
                "e intenta de nuevo. Si ya la tienes, avisa al profesor (código: $code)."

    AuthError.CredentialServiceUnavailable ->
        "El servicio de inicio de sesión de Google no está disponible en este teléfono. " +
                "Actualiza \"Servicios de Google Play\" desde Play Store, reinicia el teléfono e intenta de nuevo."

    AuthError.Interrupted ->
        "El inicio de sesión se interrumpió antes de terminar. Intenta de nuevo sin salir de la app."

    AuthError.InvalidGoogleToken ->
        "Google no pudo validar tu cuenta. Intenta de nuevo; si continúa, avisa al profesor (código: $code)."

    AuthError.NoInternet ->
        "Sin conexión a internet. Revisa tus datos o Wi-Fi e intenta de nuevo."

    AuthError.TooManyRequests ->
        "Demasiados intentos seguidos. Espera unos minutos antes de volver a intentarlo."

    AuthError.AccountDisabled ->
        "Tu cuenta está deshabilitada en Ingesoft II. Comunícate con el profesor."

    AuthError.AccountExistsWithOtherMethod ->
        "Este correo ya está registrado con otro método de acceso. " +
                "Comunícate con el profesor para unificar tu cuenta (código: $code)."

    AuthError.DomainNotAllowed ->
        "Debes iniciar sesión con tu correo institucional @unal.edu.co."

    AuthError.ProfileLoadFailed ->
        "Iniciaste sesión, pero no pudimos cargar tu perfil del curso. " +
                "Intenta de nuevo en unos minutos; si continúa, avisa al profesor (código: $code)."

    AuthError.InvalidCredentials ->
        "Correo o contraseña incorrectos."

    is AuthError.Unknown ->
        "Ocurrió un error inesperado ($detail). Intenta de nuevo; si continúa, envía una captura al profesor."
}