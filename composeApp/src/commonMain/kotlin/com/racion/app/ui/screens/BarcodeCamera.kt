package com.racion.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** `true` si la plataforma tiene cámara real para leer códigos de barras. */
expect val hasBarcodeCamera: Boolean

/**
 * Vista previa de cámara que pide el permiso al abrirse y llama a [onCode] con cada
 * código leído. En plataformas sin cámara no dibuja nada.
 */
@Composable
expect fun BarcodeCamera(modifier: Modifier, onCode: (String) -> Unit)
