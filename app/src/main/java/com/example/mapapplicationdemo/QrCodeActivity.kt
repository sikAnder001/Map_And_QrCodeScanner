package com.example.mapapplicationdemo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.mapapplicationdemo.databinding.ActivityQrCodeBinding
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.camera.core.ImageAnalysis.Analyzer
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

@ExperimentalGetImage class QrCodeActivity : AppCompatActivity() {
    lateinit var cameraSelector: CameraSelector
    lateinit var binding: ActivityQrCodeBinding

    lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    lateinit var cameraExecutor: ExecutorService

    lateinit var analyzer: MyImageAnalysis

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.window.setFlags(1024,1024)

        cameraExecutor=Executors.newSingleThreadExecutor()
        cameraProviderFuture=ProcessCameraProvider.getInstance(this)

        analyzer=MyImageAnalysis(supportFragmentManager,applicationContext)

        cameraProviderFuture.addListener({
         try {
             if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),101)
             }else{
                 val cameraProvider = cameraProviderFuture.get() as ProcessCameraProvider
                 bindPreview(cameraProvider)
             }
         } catch (e: Exception) {
            Log.d("TAG", "Use case binding failed")
        }

        }, ContextCompat.getMainExecutor(this))

    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider?) {
        val preview = Preview.Builder().build().also{
            it.setSurfaceProvider(binding.preview.surfaceProvider)
        }
        Toast.makeText(applicationContext, "bind preview", Toast.LENGTH_SHORT).show()
        val imageCapture= ImageCapture.Builder().build()

         var imageAnalysis =ImageAnalysis.Builder()
             .setTargetResolution(Size(1024,720))
             .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
             .build()

        imageAnalysis.setAnalyzer(cameraExecutor,analyzer)

        cameraSelector=CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        cameraProvider!!.unbindAll()
        cameraProvider.bindToLifecycle(this,cameraSelector,preview,imageCapture,imageAnalysis)

    }

    @ExperimentalGetImage
    class MyImageAnalysis(val fragmentManager: FragmentManager, val context: Context) : Analyzer{
        override fun analyze(image: ImageProxy) {
            scanBarCodeImage(image)
        }

        private fun scanBarCodeImage(image: ImageProxy) {
            var image1=image.image
            var inputImage= InputImage.fromMediaImage(image1!!,image.imageInfo.rotationDegrees)
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_AZTEC)
                .build()
            val scanner = BarcodeScanning.getClient(options)

            val result = scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    readerBarcodeData(barcodes)
                    // Task completed successfully
                    // ...
                }
                .addOnFailureListener {
                    // Task failed with an exception
                    // ...
                }
                .addOnCompleteListener {
                    image.close()
                }
        }

        private fun readerBarcodeData(barcodes: List<Barcode>) {
            for (barcode in barcodes) {
                val bounds = barcode.boundingBox
                val corners = barcode.cornerPoints

                val rawValue = barcode.rawValue

                val valueType = barcode.valueType
                // See API reference for complete list of supported types
                when (valueType) {
                    Barcode.TYPE_WIFI -> {
                        val ssid = barcode.wifi!!.ssid
                        val password = barcode.wifi!!.password
                        val type = barcode.wifi!!.encryptionType
                        Toast.makeText(context, "$ssid=$password=$type", Toast.LENGTH_SHORT).show()
                        Log.v("barcodeData:--","$ssid=$password=$type")
                    }
                    Barcode.TYPE_URL -> {
                        val title = barcode.url!!.title
                        val url = barcode.url!!.url
                        Toast.makeText(context, "$title=$url", Toast.LENGTH_SHORT).show()

                        Log.v("barcodeData:--","$title=$url")
                    }
                }
            }
        }

    }
}