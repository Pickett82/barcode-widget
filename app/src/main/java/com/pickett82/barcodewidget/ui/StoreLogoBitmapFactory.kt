package com.pickett82.barcodewidget.ui

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import com.pickett82.barcodewidget.data.LoyaltyCard

object StoreLogoBitmapFactory {
    fun createBadge(card: LoyaltyCard, sizePx: Int): Bitmap {
        val bitmap = createBitmap(sizePx, sizePx)
        val canvas = Canvas(bitmap)
        val background = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = card.brandColor }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = card.textColor
            textAlign = Paint.Align.CENTER
            textSize = sizePx * 0.36f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT_BOLD, android.graphics.Typeface.BOLD)
        }
        canvas.drawRoundRect(
            RectF(0f, 0f, sizePx.toFloat(), sizePx.toFloat()),
            sizePx * 0.22f,
            sizePx * 0.22f,
            background,
        )
        val y = sizePx / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(card.storeInitials.take(2), sizePx / 2f, y, textPaint)
        return bitmap
    }

    fun loadCustomLogo(context: Context, uriString: String?, sizePx: Int): Bitmap? {
        if (uriString.isNullOrBlank()) {
            return null
        }
        return runCatching {
            decodeBitmap(context.contentResolver, uriString.toUri(), sizePx)
        }.getOrNull()
    }

    private fun decodeBitmap(contentResolver: ContentResolver, uri: Uri, sizePx: Int): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri)) { decoder, _, _ ->
                decoder.setTargetSize(sizePx, sizePx)
            }
        } else {
            @Suppress("DEPRECATION")
            Bitmap.createScaledBitmap(
                MediaStore.Images.Media.getBitmap(contentResolver, uri),
                sizePx,
                sizePx,
                true,
            )
        }
    }

    fun placeholderBitmap(sizePx: Int): Bitmap {
        val bitmap = createBitmap(sizePx, sizePx)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.LTGRAY)
        return bitmap
    }
}
