package com.racion.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Desktop no tiene cámara: el visor queda como simulador.
actual val hasBarcodeCamera: Boolean = false

@Composable
actual fun BarcodeCamera(modifier: Modifier, onCode: (String) -> Unit) = Unit
