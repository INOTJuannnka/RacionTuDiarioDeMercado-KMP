package com.racion.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// shortcut: cámara solo en Android; en iOS haría falta AVCaptureSession + NSCameraUsageDescription.
actual val hasBarcodeCamera: Boolean = false

@Composable
actual fun BarcodeCamera(modifier: Modifier, onCode: (String) -> Unit) = Unit
