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
    val displayName: String? = null,
    /** Token de Firebase (dura 1 h) para llamar a Firestore como este usuario. */
    val idToken: String? = null,
    /** Permite pedir un [idToken] nuevo cuando vence. */
    val refreshToken: String? = null
) {
    /** Hay cuenta en la nube: el diario se sincroniza con Firestore. */
    val canSync: Boolean get() = userId != null && refreshToken != null

    val label: String
        get() = when (provider) {
            AuthProvider.ANONYMOUS -> "Invitado"
            else -> displayName?.takeIf { it.isNotBlank() } ?: email.orEmpty()
        }

    companion object {
        val ANONYMOUS = AuthSession(AuthProvider.ANONYMOUS)
    }
}
