package com.pickett82.barcodewidget.ui

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.pickett82.barcodewidget.data.BarcodeSymbology
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun ScannerScreen(
    modifier: Modifier = Modifier,
    onBarcodeScanned: (String, BarcodeSymbology) -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    val hasPermission = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    var cameraGranted by remember { mutableStateOf(hasPermission) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        cameraGranted = granted
    }

    LaunchedEffect(Unit) {
        if (!cameraGranted) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (cameraGranted) {
        CameraScannerPreview(
            modifier = modifier.fillMaxSize(),
            onBarcodeScanned = onBarcodeScanned,
        )
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Camera access is required to scan barcodes.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant camera access")
                }
                Button(onClick = onCancel) {
                    Text("Back to form")
                }
            }
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
private fun CameraScannerPreview(
    modifier: Modifier = Modifier,
    onBarcodeScanned: (String, BarcodeSymbology) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val executor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val handledScan = remember { AtomicBoolean(false) }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { previewView },
    ) { view ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().apply {
                    surfaceProvider = view.surfaceProvider
                }
                val scanner = BarcodeScanning.getClient(
                    BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                            Barcode.FORMAT_CODE_128,
                            Barcode.FORMAT_EAN_13,
                            Barcode.FORMAT_QR_CODE,
                            Barcode.FORMAT_UPC_A,
                            Barcode.FORMAT_UPC_E,
                            Barcode.FORMAT_ITF,
                            Barcode.FORMAT_AZTEC,
                            Barcode.FORMAT_PDF417,
                        )
                        .build(),
                )
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .apply {
                        setAnalyzer(executor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage == null || handledScan.get()) {
                                imageProxy.close()
                                return@setAnalyzer
                            }
                            val inputImage = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees,
                            )
                            scanner.process(inputImage)
                                .addOnSuccessListener { barcodes ->
                                    val first = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }
                                    if (first != null && handledScan.compareAndSet(false, true)) {
                                        onBarcodeScanned(
                                            first.rawValue.orEmpty(),
                                            first.format.toBarcodeSymbology(),
                                        )
                                    }
                                }
                                .addOnCompleteListener { imageProxy.close() }
                        }
                    }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis,
                )
            },
            ContextCompat.getMainExecutor(context),
        )
    }
}

private fun Int.toBarcodeSymbology(): BarcodeSymbology {
    return when (this) {
        Barcode.FORMAT_EAN_13 -> BarcodeSymbology.EAN_13
        Barcode.FORMAT_QR_CODE -> BarcodeSymbology.QR_CODE
        Barcode.FORMAT_UPC_A -> BarcodeSymbology.UPC_A
        Barcode.FORMAT_UPC_E -> BarcodeSymbology.UPC_E
        Barcode.FORMAT_ITF -> BarcodeSymbology.ITF
        Barcode.FORMAT_AZTEC -> BarcodeSymbology.AZTEC
        Barcode.FORMAT_PDF417 -> BarcodeSymbology.PDF_417
        else -> BarcodeSymbology.CODE_128
    }
}
