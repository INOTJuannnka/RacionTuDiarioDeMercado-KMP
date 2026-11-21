package com.racion.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.racion.app.shared.di.AppContainer
import com.racion.app.ui.theme.AppType
import com.racion.app.ui.theme.ErrorBanner
import com.racion.app.ui.theme.InfoBanner
import com.racion.app.ui.theme.PrimaryButton
import com.racion.app.ui.theme.RacionTextField
import com.racion.app.ui.theme.ScreenContainer
import com.racion.app.ui.theme.Theme
import kotlinx.coroutines.launch

/**
 * Login: correo + contraseña (Firebase), Google o invitado. Se muestra mientras no
 * haya sesión; al entrar, `AppStore.session` cambia y la raíz pasa a las pestañas.
 */
@Composable
fun LoginScreen(container: AppContainer) {
    val vm = remember { container.loginViewModel() }
    DisposableEffect(vm) { onDispose { vm.onCleared() } }
    val ui by vm.state.collectAsState()
    val google = rememberGoogleSignIn()
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Theme.colors.surface)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        ScreenContainer(bottomPadding = 40.dp) {
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .background(Theme.colors.tertiary),
                contentAlignment = Alignment.Center
            ) {
                Text("🥗", style = AppType.display)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = if (ui.isRegister) "Creá tu cuenta" else "Bienvenido a Ración",
                style = AppType.headlineLarge,
                color = Theme.colors.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))

            RacionTextField(
                value = ui.email,
                onValueChange = vm::onEmailChange,
                placeholder = "Correo electrónico",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(Modifier.height(10.dp))
            RacionTextField(
                value = ui.password,
                onValueChange = vm::onPasswordChange,
                placeholder = "Contraseña",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation()
            )
            if (ui.isRegister) {
                Spacer(Modifier.height(10.dp))
                RacionTextField(
                    value = ui.confirmPassword,
                    onValueChange = vm::onConfirmChange,
                    placeholder = "Confirmar contraseña",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation()
                )
            }

            ui.errorMessage?.let { ErrorBanner(it, Modifier.padding(top = 12.dp)) }
            ui.infoMessage?.let { InfoBanner(it, Modifier.padding(top = 12.dp)) }

            Spacer(Modifier.height(18.dp))
            PrimaryButton(
                title = if (ui.isRegister) "Crear cuenta" else "Iniciar sesión",
                isLoading = ui.isLoading,
                onClick = vm::submit
            )

            LinkText(
                text = if (ui.isRegister) "Ya tengo cuenta · Iniciar sesión" else "¿No tenés cuenta? Registrate",
                onClick = vm::toggleMode
            )
            if (!ui.isRegister) {
                LinkText(text = "Olvidé mi contraseña", onClick = vm::sendPasswordReset)
            }

            Spacer(Modifier.height(18.dp))
            Text(
                text = "o",
                style = AppType.bodyMedium,
                color = Theme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlineButton(
                title = "G  Continuar con Google",
                enabled = !ui.isLoading,
                onClick = {
                    if (google == null) {
                        vm.showError("Google todavía no está disponible en esta plataforma. Usá correo o invitado.")
                    } else {
                        scope.launch {
                            google().onSuccess { token -> token?.let(vm::signInWithGoogle) }
                                .onFailure { vm.showError(it.message ?: "No se pudo iniciar sesión con Google.") }
                        }
                    }
                }
            )
            Spacer(Modifier.height(10.dp))
            OutlineButton(
                title = "👤  Continuar como anónimo",
                enabled = !ui.isLoading,
                onClick = vm::continueAsGuest
            )
            Text(
                text = "Como anónimo tus datos quedan solo en este dispositivo.",
                style = AppType.labelSmall,
                color = Theme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun LinkText(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = AppType.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        color = Theme.colors.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(top = 14.dp, bottom = 2.dp)
    )
}

@Composable
private fun OutlineButton(title: String, enabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(shape)
            .background(Theme.colors.surfaceContainerLowest)
            .border(1.dp, Theme.colors.outlineVariant, shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(title, style = AppType.titleMedium, color = Theme.colors.onSurface)
    }
}
