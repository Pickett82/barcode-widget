package com.example.barcodewidget.ui.screens

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.barcodewidget.BarcodeWidgetApp
import com.example.barcodewidget.ui.viewmodel.BarcodeDetailViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeDetailScreen(
    cardId: Long,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as BarcodeWidgetApp
    val viewModel: BarcodeDetailViewModel = viewModel(
        factory = BarcodeDetailViewModel.Factory(app.container.repository, cardId)
    )
    val card by viewModel.card.collectAsState()
    var barcodeBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(card) {
        val c = card ?: return@LaunchedEffect
        barcodeBitmap = withContext(Dispatchers.Default) {
            renderBarcode(c.barcodeValue, c.barcodeFormat)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(card?.storeName ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (card == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val c = card!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val logoResId = c.logoResName?.let { resName ->
                    context.resources.getIdentifier(resName, "drawable", context.packageName)
                } ?: 0

                when {
                    c.customLogoUri != null -> {
                        AsyncImage(
                            model = c.customLogoUri,
                            contentDescription = c.storeName,
                            modifier = Modifier.size(72.dp)
                        )
                    }
                    logoResId != 0 -> {
                        Image(
                            painter = painterResource(id = logoResId),
                            contentDescription = c.storeName,
                            modifier = Modifier.size(72.dp)
                        )
                    }
                }

                Text(text = c.storeName, style = MaterialTheme.typography.headlineMedium)

                Spacer(modifier = Modifier.height(8.dp))

                if (barcodeBitmap != null) {
                    Image(
                        bitmap = barcodeBitmap!!.asImageBitmap(),
                        contentDescription = "Barcode",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                Text(
                    text = c.barcodeValue,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = c.barcodeFormat,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun renderBarcode(value: String, format: String): Bitmap? {
    return try {
        val barcodeFormat = BarcodeFormat.valueOf(format)
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val (width, height) = if (barcodeFormat == BarcodeFormat.QR_CODE ||
            barcodeFormat == BarcodeFormat.DATA_MATRIX ||
            barcodeFormat == BarcodeFormat.AZTEC
        ) {
            Pair(512, 512)
        } else {
            Pair(900, 300)
        }
        val bitMatrix = MultiFormatWriter().encode(value, barcodeFormat, width, height, hints)
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        bmp.setPixels(pixels, 0, width, 0, 0, width, height)
        bmp
    } catch (e: Exception) {
        null
    }
}
