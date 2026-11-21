package com.racion.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.racion.app.shared.network.FirebaseAuthService

/** Google con Credential Manager. El contexto debe ser la Activity para mostrar el selector. */
@Composable
actual fun rememberGoogleSignIn(): (suspend () -> Result<String?>)? {
    val context = LocalContext.current
    return remember(context) {
        suspend {
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(
                    GetSignInWithGoogleOption.Builder(FirebaseAuthService.GOOGLE_WEB_CLIENT_ID).build()
                )
                .build()
            try {
                val credential = CredentialManager.create(context).getCredential(context, request).credential
                Result.success(GoogleIdTokenCredential.createFrom(credential.data).idToken)
            } catch (_: GetCredentialCancellationException) {
                Result.success(null)
            } catch (_: NoCredentialException) {
                Result.failure(Exception("No hay una cuenta de Google en este dispositivo. Agregá una en Ajustes."))
            } catch (e: GetCredentialException) {
                Result.failure(Exception("No se pudo iniciar sesión con Google: ${e.message ?: e.type}"))
            }
        }
    }
}
