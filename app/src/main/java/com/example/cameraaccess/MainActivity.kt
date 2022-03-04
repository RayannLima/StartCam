package com.example.cameraaccess

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.example.cameraaccess.databinding.ActivityMainBinding
import com.example.cameraaccess.modulos.StartCamScan
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode.*
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val REQUEST_IMAGE_CAPTURE = 1
    lateinit var currentPhotoPath: String
    private lateinit var uriPath: Uri

    private val barcodeLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            val originalIntent = result.originalIntent
            if (originalIntent == null) {
                Log.d("MainActivity", "Cancelled scan")
                Toast.makeText(this@MainActivity, "Cancelled", Toast.LENGTH_LONG).show()
            } else if (originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                Log.d(
                    "MainActivity",
                    "Cancelled scan due to missing camera permission"
                )
                Toast.makeText(
                    this@MainActivity,
                    "Cancelled due to missing camera permission",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            binding.textViewScanner.text = result.contents
            Log.d("MainActivity", "Scanned")
            Toast.makeText(
                this@MainActivity,
                "Scanned: " + result.contents,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)


        binding.buttonTakePicture.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    0
                )
                return@setOnClickListener
            }
            takePictureIntent()


        }

        binding.buttonScanCode.setOnClickListener {
            StartCamScan().scanBarCode(barcodeLauncher)
        }

        setContentView(binding.root)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                var file: File = File(currentPhotoPath)
                val imageBitmap = Orientation().loadImage(file.absolutePath)
                binding.imageView.setImageBitmap(imageBitmap)
                readCodebar(imageBitmap!!)
                //  identifyBarCode(imageBitmap!!)

            } catch (error: IOException) {
                Toast.makeText(
                    this,
                    "Nao foi possivel gravar a imagem neste device",
                    Toast.LENGTH_LONG
                ).show()
                Log.d("Cam", "Nao foi possivel grava a imagem no Device ")
            }
        }

        binding.textViewScanner.text = StartCamScan().listenerQrCode(requestCode, resultCode, data)
    }

    private fun identifyBarCode(imageBitmap: Bitmap) {
        val detector = BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.CODE_39).build()
        if (!detector.isOperational) {
            binding.textViewScanner.text = "Não foi possivel escaner codigo"
        } else {
            val frame = Frame.Builder().setBitmap(imageBitmap).build()
            val codeBar = detector.detect(frame)

            val codeText = codeBar.valueAt(0)
            binding.textViewScanner.text = codeText.rawValue
        }

    }

    fun readCodebar(imageBitmap: Bitmap) {
        val optionsScan = BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.CODE_39).build()
        val image = InputImage.fromBitmap(imageBitmap, 0)
        val scanner = BarcodeScanning.getClient(optionsScan)
        val result = scanner.process(image)
            .addOnSuccessListener { barCodes ->
                for (barcode in barCodes) {
                    val bounds = barcode.boundingBox
                    val corners = barcode.cornerPoints

                    val rawValue = barcode.rawValue

                    val valueType = barcode.valueType
                    binding.textViewScanner.text = rawValue
                    // See API reference for complete list of supported types
                    when (valueType) {
                        Barcode.WIFI -> {
                            val ssid = barcode.wifi!!.ssid
                            val password = barcode.wifi!!.password
                            val type = barcode.wifi!!.encryptionType
                        }
                        Barcode.URL -> {
                            val title = barcode.url!!.title
                            val url = barcode.url!!.url
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Nao foi possivel analisar codebar", Toast.LENGTH_LONG).show()
            }

    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun takePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Toast.makeText(this, "Nao foi possivel salvar foto", Toast.LENGTH_LONG).show()
                    return
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        applicationContext.packageName + ".provider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }
}