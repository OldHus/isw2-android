package com.svape.unal.ingesoftii.domain.model

@Suppress("JavaIoSerializableObjectMustHaveReadResolve", "<nombre que inserte el IDE>")
sealed class AuthError(val code: String, message: String) : Exception(message) {
    data object CancelledByUser : AuthError("cancelled", "Inicio de sesión con Google cancelado")
    data object NoGoogleAccountAvailable : AuthError("no_credentials", "No hay credenciales de Google disponibles")
    data object CredentialServiceUnavailable : AuthError("credential_service", "Servicio de credenciales no disponible")
    data object Interrupted : AuthError("interrupted", "El inicio de sesión se interrumpió")
    data object InvalidGoogleToken : AuthError("invalid_token", "El token de Google fue rechazado")
    data object NoInternet : AuthError("no_internet", "Sin conexión a internet")
    data object TooManyRequests : AuthError("too_many_requests", "Demasiados intentos de inicio de sesión")
    data object AccountDisabled : AuthError("account_disabled", "La cuenta está deshabilitada")
    data object AccountExistsWithOtherMethod : AuthError("account_collision", "La cuenta existe con otro método de acceso")
    data object DomainNotAllowed : AuthError("domain_not_allowed", "Dominio de correo no permitido")
    data object ProfileLoadFailed : AuthError("profile_load", "No se pudo cargar el perfil del usuario")
    data object InvalidCredentials : AuthError("invalid_credentials", "Correo o contraseña incorrectos")
    class Unknown(val detail: String) : AuthError("unknown", detail)
}