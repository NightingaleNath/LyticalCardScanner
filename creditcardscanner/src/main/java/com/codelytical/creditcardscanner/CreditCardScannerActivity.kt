package com.codelytical.creditcardscanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.codelytical.creditcardscanner.databinding.ActivityCreditCardScannerBinding
import com.codelytical.creditcardscanner.library.SdkActivityLauncher
import com.codelytical.creditcardscanner.library.SdkActivityType
import com.codelytical.creditcardscanner.library.ViewToggleHelper
import com.codelytical.creditcardscanner.model.CardDetails
import com.codelytical.creditcardscanner.usecase.ExtractDataUseCase
import com.codelytical.creditcardscanner.utils.CAMERA_PERMISSION
import com.codelytical.creditcardscanner.utils.getCameraProvider
import com.codelytical.creditcardscanner.utils.hasPermission
import com.codelytical.creditcardscanner.utils.hideStatusBar
import com.codelytical.creditcardscanner.utils.launchWhenResumed
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch

@ExperimentalGetImage @SuppressLint("UnsafeExperimentalUsageError")
class CreditCardScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreditCardScannerBinding

    private var isTorchOn = false

    private var camera: Camera? = null

    private val useCase = ExtractDataUseCase(TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS))
    private var hasCapturedCardDetails = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreditCardScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideStatusBar(this)
        
        cameraPermissions()

        binding.wocrIvFlashId.setOnClickListener {
            toggleFlashlight()
        }

        binding.wocrIvRefresh.setOnClickListener {
            resetScanningProcess()
        }

        binding.wocrIvClose.setOnClickListener {
            finish()
        }

        binding.wocrTvEnterCardNumberId.setOnClickListener {
            finish()
        }

        binding.wocrTvEnterCardNumberId.setOnClickListener {
            SdkActivityLauncher.saveResult("", "", "", cardEdit = true)
            finish()
        }

    }

    private fun resetScanningProcess() {
        launchWhenResumed {
            val cameraProvider = getCameraProvider()
            cameraProvider.unbindAll()
        }

        hasCapturedCardDetails = false

        binding.previewViewContainer.wocrCardDetectionState.clearRecognitionResult()

        binding.previewViewContainer.confirmImage.visibility = View.GONE

        launchWhenResumed {
            bindUseCases(getCameraProvider())
        }
    }

    private fun toggleFlashlight() {
        camera?.let {
            if (it.cameraInfo.hasFlashUnit()) {
                isTorchOn = !isTorchOn // Toggle the torch state
                it.cameraControl.enableTorch(isTorchOn)
            }
        } ?: Log.e("CreditCardScanner", "Camera not initialized")
    }


    @SuppressLint("UnsafeOptInUsageError")
    private fun cameraPermissions() {
        if (hasPermission(Manifest.permission.CAMERA)
        ) {
            launchWhenResumed {
                bindUseCases(getCameraProvider())
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION)
            }
        }
    }

    @ExperimentalGetImage
    private fun bindUseCases(cameraProvider: ProcessCameraProvider) {
        val preview = buildPreview()
        val imageAnalysis = buildImageAnalysis()
        val cameraSelector = buildCameraSelector()

        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
    }

    private fun buildPreview(): Preview = Preview.Builder()
        .build()
        .apply {
            setSurfaceProvider(binding.previewViewContainer.cameraPreview.surfaceProvider)
        }

    private fun buildCameraSelector(): CameraSelector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build()

    private fun buildImageAnalysis(): ImageAnalysis {
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                lifecycleScope.launch {
                    val cardDetails = useCase(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    if (!hasCapturedCardDetails && cardDetails.number != null && cardDetails.expirationMonth != null) {
                        runOnUiThread {
                            bindCardDetails(cardDetails)
                        }
                        hasCapturedCardDetails = true
                    }

                    imageProxy.close() // Always close the imageProxy
                }
            } else {
                imageProxy.close() // Close the imageProxy if not processed
            }
        }
        return imageAnalysis
    }

    @SuppressLint("SetTextI18n")
    private fun bindCardDetails(card: CardDetails) {
       val cardNumber = card.number
        val expiryDate = "${card.expirationMonth}/${card.expirationYear}"

        println("SCANNED RECORDS HERE: $cardNumber, $expiryDate")

        binding.previewViewContainer.wocrCardDetectionState.setRecognitionResult(card)

        ViewToggleHelper.toggleViewSupport(binding.previewViewContainer.confirmImage)

        if (ViewToggleHelper.showConfirm) {
            binding.previewViewContainer.confirmImage.setOnClickListener {
                returnResult(cardNumber, expiryDate)
            }
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                if (!isFinishing) {
                    returnResult(cardNumber, expiryDate)
                }
            }, 700)
        }
    }

    private fun returnResult(cardNumber: String?, expiryDate: String?) {
        SdkActivityLauncher.saveResult(cardNumber, expiryDate, "", cardEdit = true)
        finish()
    }


    @SuppressLint("UnsafeOptInUsageError")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchWhenResumed {
                bindUseCases(getCameraProvider())
            }
        }
    }

}