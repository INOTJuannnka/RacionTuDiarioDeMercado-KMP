package com.racion.app.ui.screens

import android.util.Log
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

private const val TAG = "RacionGoogle"

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
            } catch (e: GetCredentialCancellationException) {
                // Google también reporta "cancelado" cuando la app no está registrada (SHA-1), así que se avisa.
                Log.w(TAG, "Google cancelado: ${e.message}", e)
                Result.failure(Exception("No se completó el inicio con Google. Si elegiste tu cuenta, falta registrar la app (SHA-1) en Firebase."))
            } catch (e: NoCredentialException) {
                Log.w(TAG, "Sin credencial de Google: ${e.message}", e)
                Result.failure(Exception("No hay una cuenta de Google en este dispositivo. Agregá una en Ajustes."))
            } catch (e: GetCredentialException) {
                Log.w(TAG, "Fallo de Credential Manager: ${e.type} ${e.message}", e)
                Result.failure(Exception("No se pudo iniciar sesión con Google: ${e.message ?: e.type}"))
            }
        }
    }
}
