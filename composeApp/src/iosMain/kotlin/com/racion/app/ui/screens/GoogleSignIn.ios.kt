package com.racion.app.ui.screens

import androidx.compose.runtime.Composable

// shortcut: Google solo en Android; en iOS haría falta GoogleSignIn-iOS en el host Swift.
@Composable
actual fun rememberGoogleSignIn(): (suspend () -> Result<String?>)? = null
