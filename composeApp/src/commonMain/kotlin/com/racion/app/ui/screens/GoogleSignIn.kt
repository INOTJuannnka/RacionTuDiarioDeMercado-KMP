package com.racion.app.ui.screens

import androidx.compose.runtime.Composable

/**
 * Pide a la plataforma un ID token de Google.
 * Devuelve `null` si la plataforma no soporta Google (Desktop / iOS por ahora).
 * El resultado: token, `null` si la persona canceló, o un fallo con mensaje para mostrar.
 */
@Composable
expect fun rememberGoogleSignIn(): (suspend () -> Result<String?>)?
