package com.racion.app.ui.screens

import androidx.compose.runtime.Composable

// shortcut: Google solo en Android (Credential Manager); en Desktop haría falta un flujo OAuth por navegador.
@Composable
actual fun rememberGoogleSignIn(): (suspend () -> Result<String?>)? = null
