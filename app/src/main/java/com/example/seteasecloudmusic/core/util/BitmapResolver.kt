package com.example.seteasecloudmusic.core.util

import android.graphics.Bitmap

object BitmapResolver {
    fun bitmapCompress(bitmap: Bitmap, targetSize: Int = 96): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height

        val size = minOf(originalWidth, originalHeight)
        val xOffset = (originalWidth - size) / 2
        val yOffset = (originalHeight - size) / 2
        val squareBitmap = Bitmap.createBitmap(bitmap, xOffset, yOffset, size, size)

        val compressedBitmap = if (size > targetSize) {
            val scaleFactor = size / targetSize
            val scaledSize = size / scaleFactor
            Bitmap.createScaledBitmap(squareBitmap, scaledSize, scaledSize, true)
        } else {
            squareBitmap
        }

        return compressedBitmap.copy(Bitmap.Config.RGB_565, false)
    }
}
