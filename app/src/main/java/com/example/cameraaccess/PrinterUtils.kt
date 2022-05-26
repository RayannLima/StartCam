package com.example.moduleprinter.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.moduleprinter.enums.IntensityLevel
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class PrinterUtils(private val context: Context) {

    init {
        if (!OpenCVLoader.initDebug()) {
            Log.d(
                "OpenCV",
                "Internal OpenCV library not found. Using OpenCV Manager for initialization"
            )
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!")
            loadLibOpenCV(context)
        }
    }

    fun toInvertedMonochromatic(bitmap: Bitmap?, factor: Double): Bitmap? {
        return toInvertedMonochromatic(bitmap, factor, false)
    }

    fun toInvertedMonochromatic(
        bitmap: Bitmap?,
        factor: Double,
        removeExtraBlack: Boolean
    ): Bitmap? {
        val matOutput = Mat()
        val matGrayscale = buildGrayscale(bitmap!!)
        val threshold = (factor * 255).toInt()
        Imgproc.threshold(
            matGrayscale,
            matOutput,
            threshold.toDouble(),
            255.0,
            Imgproc.THRESH_BINARY_INV
        )
        val monochromaticBitmap = matToBitmap(bitmap, matOutput)
        return if (removeExtraBlack) adaptiveThreshMean(monochromaticBitmap) else monochromaticBitmap
    }

    fun applyIntensityLevel(monochromaticBitmap: Bitmap?, intensityLevel: Int): Bitmap? {
        val level: Int
        if (intensityLevel < IntensityLevel.INTENSITY_LEVEL_1.code) {
            level = IntensityLevel.INTENSITY_LEVEL_1.code
        } else if (intensityLevel > IntensityLevel.INTENSITY_LEVEL_5.code) {
            level = IntensityLevel.INTENSITY_LEVEL_5.code
        } else {
            level = intensityLevel
        }
        val kernelSize = Size(
            level.toDouble(),
            level.toDouble()
        )
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, kernelSize)
        val matInput = bitmapToMat(monochromaticBitmap!!)
        val matOutput = Mat()
        Imgproc.erode(matInput, matOutput, kernel)
        return matToBitmap(monochromaticBitmap, matOutput)
    }

    fun toMonochromatic(bitmap: Bitmap, factor: Double): Bitmap {
        return toMonochromatic(bitmap, factor, false)
    }


    fun toMonochromatic(bitmap: Bitmap, factor: Double, removeExtraBlack: Boolean): Bitmap {
        var matOutput = Mat()
        var matGrayscale = buildGrayscale(bitmap)

        val threshold = (factor * 255)

        Imgproc.threshold(matGrayscale, matOutput, threshold, 255.00, Imgproc.THRESH_BINARY)
        val monochromaticBitmap = matToBitmap(bitmap, matOutput)
        return if (removeExtraBlack) adaptiveThreshMean(monochromaticBitmap) else monochromaticBitmap
    }

    private fun threshold(bitmap: Bitmap): Bitmap? {
        val matOutput = Mat()
        val matGrayscale = buildGrayscale(bitmap)
        Imgproc.threshold(matGrayscale, matOutput, 127.0, 255.0, Imgproc.THRESH_BINARY)
        return matToBitmap(bitmap, matOutput)
    }

    private fun adaptiveThreshGaussian(bitmap: Bitmap): Bitmap? {
        val matGrayscale = buildGrayscale(bitmap)
        val matOutput = Mat()
        Imgproc.adaptiveThreshold(
            matGrayscale,
            matOutput,
            255.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY,
            11,
            2.0
        )
        return matToBitmap(bitmap, matOutput)
    }

    private fun thresholdOtsu(bitmapEntry: Bitmap): Bitmap? {
        val matOutput = Mat()
        val matGrayscale = buildGrayscale(bitmapEntry)
        Imgproc.threshold(
            matGrayscale,
            matOutput,
            0.0,
            255.0,
            Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU
        )
        return matToBitmap(bitmapEntry, matOutput)
    }

    private fun thresholdInv(bitmapEntry: Bitmap): Bitmap? {
        val matOutput = Mat()
        val matGrayscale = buildGrayscale(bitmapEntry)
        Imgproc.threshold(matGrayscale, matOutput, 127.0, 255.0, Imgproc.THRESH_BINARY_INV)
        return matToBitmap(bitmapEntry, matOutput)
    }

    private fun adaptiveThreshMean(bitmap: Bitmap): Bitmap {
        val matOutput = Mat()
        val matGrayscale = buildGrayscale(bitmap)
        Imgproc.adaptiveThreshold(
            matGrayscale,
            matOutput,
            255.0,
            Imgproc.ADAPTIVE_THRESH_MEAN_C,
            Imgproc.THRESH_BINARY,
            11,
            2.0
        )
        return matToBitmap(bitmap, matOutput)
    }

    private fun matToBitmap(bitmapEntry: Bitmap, matEntry: Mat): Bitmap {
        val btmOut =
            Bitmap.createBitmap(bitmapEntry.width, bitmapEntry.height, Bitmap.Config.RGB_565)
        Utils.matToBitmap(matEntry, btmOut)
        return btmOut
    }


    private fun buildGrayscale(bitmap: Bitmap): Mat? {
        var matGrayscale = Mat()
        Imgproc.cvtColor(bitmapToMat(bitmap), matGrayscale, Imgproc.COLOR_BGR2GRAY)
        return matGrayscale
    }

    private fun bitmapToMat(bitmap: Bitmap): Mat? {
        val bitmapToMat = Mat()
        Utils.bitmapToMat(bitmap, bitmapToMat)
        return bitmapToMat
    }

    private fun loadLibOpenCV(context: Context) {

        object : BaseLoaderCallback(context) {
            override fun onManagerConnected(status: Int) {
                if (status == LoaderCallbackInterface.SUCCESS) {
                    Log.d("OpenCV", "OpenCV loaded successfully")
                } else {
                    super.onManagerConnected(status)

                }
            }
        }
    }

}

