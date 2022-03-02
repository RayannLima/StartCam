package com.example.cameraaccess

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.IOException

class Orientation {

    @Throws(IOException::class)
    fun loadImage(caminhoFoto: String?): Bitmap? {
        val image = BitmapFactory.decodeFile(caminhoFoto)
        val exif = ExifInterface(caminhoFoto!!)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_ROTATE_90
        )
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> return rotate(image, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> return rotate(image, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> return rotate(image, 270f)
        }
        return image
    }

    private fun rotate(bmp: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            bmp, 0, 0, bmp.width, bmp.height, matrix, true
        )
    }
}