package com.racion.app.shared.models

import kotlinx.serialization.Serializable

/** Cómo entró la persona. Define qué se muestra en "Cuenta". */
@Serializable
enum class AuthProvider { EMAIL, GOOGLE, ANONYMOUS }

/**
 * Sesión activa. Un invitado ([AuthProvider.ANONYMOUS]) no tiene `userId`: sus datos
 * viven solo en este dispositivo.
 */
@Serializable
data class AuthSession(
    val provider: AuthProvider,
    val userId: String? = null,
    val email: String? = null,
    val displayName: String? = null
) {
    val label: String
        get() = when (provider) {
            AuthProvider.ANONYMOUS -> "Invitado"
            else -> displayName?.takeIf { it.isNotBlank() } ?: email.orEmpty()
        }

    companion object {
        val ANONYMOUS = AuthSession(AuthProvider.ANONYMOUS)
    }
}
