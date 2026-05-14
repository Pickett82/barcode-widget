package com.pickett82.barcodewidget.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.pickett82.barcodewidget.data.BarcodeSymbology

object BarcodeBitmapFactory {
    fun create(
        value: String,
        format: BarcodeSymbology,
        width: Int,
        height: Int,
    ): Bitmap {
        val safeWidth = width.coerceAtLeast(64)
        val safeHeight = height.coerceAtLeast(64)
        val bitMatrix = MultiFormatWriter().encode(
            value,
            format.toZxing(),
            safeWidth,
            safeHeight,
            mapOf(EncodeHintType.MARGIN to 1),
        )
        return if (format == BarcodeSymbology.QR_CODE || format == BarcodeSymbology.AZTEC) {
            bitMatrix.toBitmap()
        } else {
            drawLinearBarcode(bitMatrix, safeWidth, safeHeight)
        }
    }

    private fun drawLinearBarcode(
        matrix: BitMatrix,
        width: Int,
        height: Int,
    ): Bitmap {
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        val paint = Paint().apply { color = Color.BLACK }
        val bottom = (height * 0.82f)
        for (x in 0 until matrix.width) {
            if (matrix.get(x, matrix.height / 2)) {
                val scaledX = x * width / matrix.width.toFloat()
                val nextX = (x + 1) * width / matrix.width.toFloat()
                canvas.drawRect(scaledX, 0f, nextX, bottom, paint)
            }
        }
        return bitmap
    }

    private fun BitMatrix.toBitmap(): Bitmap {
        val bitmap = createBitmap(width, height)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    private fun BarcodeSymbology.toZxing(): BarcodeFormat {
        return when (this) {
            BarcodeSymbology.CODE_128 -> BarcodeFormat.CODE_128
            BarcodeSymbology.EAN_13 -> BarcodeFormat.EAN_13
            BarcodeSymbology.QR_CODE -> BarcodeFormat.QR_CODE
            BarcodeSymbology.UPC_A -> BarcodeFormat.UPC_A
            BarcodeSymbology.UPC_E -> BarcodeFormat.UPC_E
            BarcodeSymbology.ITF -> BarcodeFormat.ITF
            BarcodeSymbology.AZTEC -> BarcodeFormat.AZTEC
            BarcodeSymbology.PDF_417 -> BarcodeFormat.PDF_417
        }
    }
}
