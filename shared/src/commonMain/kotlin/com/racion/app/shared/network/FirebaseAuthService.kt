package com.racion.app.shared.network

import com.racion.app.shared.models.AppError
import com.racion.app.shared.models.AppResult
import com.racion.app.shared.models.AuthProvider
import com.racion.app.shared.models.AuthSession
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable

/**
 * Firebase Auth por su API REST (Identity Toolkit), con el mismo proyecto que usaba
 * la entrega SwiftUI. Va por Ktor para que el mismo código sirva en Android, iOS y
 * Desktop sin el SDK nativo de Firebase.
 */
class FirebaseAuthService(
    private val client: HttpClient,
    private val apiKey: String = FIREBASE_API_KEY
) {

    suspend fun signIn(email: String, password: String): AppResult<AuthSession> =
        call("accounts:signInWithPassword", PasswordRequest(normalize(email), password), AuthProvider.EMAIL)

    suspend fun signUp(email: String, password: String): AppResult<AuthSession> =
        call("accounts:signUp", PasswordRequest(normalize(email), password), AuthProvider.EMAIL)

    /** Cambia el ID token que entrega Google (Credential Manager) por una sesión de Firebase. */
    suspend fun signInWithGoogle(idToken: String): AppResult<AuthSession> =
        call(
            "accounts:signInWithIdp",
            IdpRequest(postBody = "id_token=$idToken&providerId=google.com"),
            AuthProvider.GOOGLE
        )

    suspend fun sendPasswordReset(email: String): AppResult<Unit> =
        call("accounts:sendOobCode", OobRequest(email = normalize(email)), AuthProvider.EMAIL).map { }

    private suspend inline fun <reified B : Any> call(
        endpoint: String,
        body: B,
        provider: AuthProvider
    ): AppResult<AuthSession> = try {
        val response = client.post("$BASE_URL/$endpoint?key=$apiKey") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        if (response.status.isSuccess()) {
            val auth = response.body<AuthResponse>()
            AppResult.Success(AuthSession(provider, auth.localId, auth.email, auth.displayName))
        } else {
            val raw = response.bodyAsText()
            println("RacionAuth $endpoint HTTP ${response.status.value}: $raw")
            val message = runCatching { authJson.decodeFromString<ErrorResponse>(raw).error?.message }.getOrNull()
            AppResult.Failure(mapFirebaseError(message))
        }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        AppResult.Failure(AppError.Network)
    }

    private fun normalize(email: String) = email.trim().lowercase()

    @Serializable
    private data class PasswordRequest(
        val email: String,
        val password: String,
        val returnSecureToken: Boolean = true
    )

    @Serializable
    private data class IdpRequest(
        val postBody: String,
        val requestUri: String = "http://localhost",
        val returnSecureToken: Boolean = true,
        val returnIdpCredential: Boolean = true
    )

    @Serializable
    private data class OobRequest(val email: String, val requestType: String = "PASSWORD_RESET")

    @Serializable
    private data class AuthResponse(
        val localId: String? = null,
        val email: String? = null,
        val displayName: String? = null
    )

    @Serializable
    private data class ErrorResponse(val error: ErrorBody? = null)

    @Serializable
    private data class ErrorBody(val message: String? = null)

    companion object {
        private val authJson = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
        private const val BASE_URL = "https://identitytoolkit.googleapis.com/v1"

        // Clave pública de cliente del proyecto `racion-tu-diario-de-mercado` (la misma de
        // GoogleService-Info.plist). No es un secreto: las reglas de Firebase protegen los datos.
        const val FIREBASE_API_KEY = "AIzaSyCM0R--T2SYVjfQNUccIpGkzGipRi-LcK4"

        /** Cliente OAuth "web" del proyecto; Credential Manager lo pide como `serverClientId`. */
        const val GOOGLE_WEB_CLIENT_ID =
            "869030106464-ninnljm6tccrtnt1359cgmv2scv53d4e.apps.googleusercontent.com"
    }
}

/** Traduce el código de error de Identity Toolkit (p. ej. "INVALID_LOGIN_CREDENTIALS") a español. */
internal fun mapFirebaseError(code: String?): AppError {
    // Firebase a veces agrega detalle: "WEAK_PASSWORD : Password should be at least 6 characters".
    val key = code?.substringBefore(' ')?.substringBefore(':')
    return when (key) {
        "INVALID_EMAIL" -> AppError.Unauthorized("El correo no tiene un formato válido.")
        "INVALID_LOGIN_CREDENTIALS", "INVALID_PASSWORD" ->
            AppError.Unauthorized("Correo o contraseña incorrectos.")
        "EMAIL_NOT_FOUND" -> AppError.Unauthorized("No existe una cuenta con ese correo.")
        "EMAIL_EXISTS" -> AppError.Unauthorized("Ya existe una cuenta con ese correo.")
        "WEAK_PASSWORD" -> AppError.Unauthorized("La contraseña es demasiado débil (mínimo 6 caracteres).")
        "USER_DISABLED" -> AppError.Unauthorized("Esta cuenta fue deshabilitada.")
        "OPERATION_NOT_ALLOWED" -> AppError.Unauthorized("Este método de inicio de sesión no está habilitado.")
        "INVALID_IDP_RESPONSE" -> AppError.Unauthorized("Google no validó el inicio de sesión. Intentá de nuevo.")
        "TOO_MANY_ATTEMPTS_TRY_LATER" -> AppError.RateLimited
        else -> AppError.Unauthorized("No se pudo iniciar sesión (${code ?: "sin detalle"}).")
    }
}
