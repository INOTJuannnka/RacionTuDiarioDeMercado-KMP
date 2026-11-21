package com.racion.app.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.racion.app.ui.theme.AppType
import com.racion.app.ui.theme.Theme

actual val hasBarcodeCamera: Boolean = true

@Composable
actual fun BarcodeCamera(modifier: Modifier, onCode: (String) -> Unit) {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = it
    }
    LaunchedEffect(Unit) {
        if (!granted) launcher.launch(Manifest.permission.CAMERA)
    }
    // Al volver de Ajustes el permiso pudo cambiar.
    LifecycleResumeEffect(Unit) {
        granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        onPauseOrDispose { }
    }

    if (!granted) {
        val activity = context as? Activity
        Box(
            modifier.clickable {
                // Tras "No volver a preguntar" Android ya no muestra el diálogo: se abre Ajustes.
                val canAsk = activity == null ||
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)
                if (canAsk) launcher.launch(Manifest.permission.CAMERA)
                else context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                )
            },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Permití el acceso a la cámara para escanear.\nTocá aquí para conceder el permiso.",
                style = AppType.labelSmall,
                color = Theme.colors.cameraTextMuted,
                modifier = Modifier.padding(24.dp)
            )
        }
        return
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnCode by rememberUpdatedState(onCode)
    val controller = remember { LifecycleCameraController(context) }

    DisposableEffect(controller, lifecycleOwner) {
        val executor = ContextCompat.getMainExecutor(context)
        val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E
                )
                .build()
        )
        // El analizador entrega el mismo código en cada cuadro: se reenvía solo si cambia o pasaron 3 s.
        var lastCode: String? = null
        var lastAt = 0L
        controller.setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
        controller.setImageAnalysisAnalyzer(
            executor,
            MlKitAnalyzer(listOf(scanner), ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL, executor) { result ->
                val code = result.getValue(scanner)?.firstOrNull()?.rawValue ?: return@MlKitAnalyzer
                val now = System.currentTimeMillis()
                if (code != lastCode || now - lastAt > 3_000) {
                    lastCode = code
                    lastAt = now
                    currentOnCode(code)
                }
            }
        )
        controller.bindToLifecycle(lifecycleOwner)
        onDispose {
            controller.unbind()
            scanner.close()
        }
    }

    AndroidView(
        factory = { PreviewView(it).apply { this.controller = controller } },
        modifier = modifier
    )
}
