package com.example.cameraaccess.modulos


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.result.ActivityResultLauncher
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class StartCamScan {


    fun initScan(activity: Activity) {
        IntentIntegrator(activity).apply {
            setBeepEnabled(false)
            initiateScan()
        }
    }

    fun scanBarCode(barcodeLaucher: ActivityResultLauncher<ScanOptions>) {
        val scanOptions: ScanOptions = ScanOptions()

        scanOptions.setBarcodeImageEnabled(true)
        scanOptions.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
        scanOptions.setCameraId(0)
        scanOptions.setBeepEnabled(false)
        barcodeLaucher.launch(scanOptions)


    }


    fun listenerQrCode(requestCode: Int, resultCode: Int, data: Intent?): String? {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        return result?.contents?.toString()
    }

    fun createQrCode(dado: String, largura: Int = 400, altura: Int = 400): Bitmap? {
        return try {
            BarcodeEncoder().encodeBitmap(dado, BarcodeFormat.QR_CODE, largura, altura)
        } catch (e: WriterException) {
            null
        }
    }

}