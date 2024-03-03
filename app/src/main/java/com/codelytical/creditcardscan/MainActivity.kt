package com.codelytical.creditcardscan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.codelytical.creditcardscan.databinding.ActivityMainBinding
import com.codelytical.creditcardscanner.CreditCardScannerActivity
import com.codelytical.creditcardscanner.NfcReadActivity
import com.codelytical.creditcardscanner.library.SdkActivityLauncher
import com.codelytical.creditcardscanner.library.SdkActivityType
import com.codelytical.creditcardscanner.library.SdkResultCallback
import com.codelytical.creditcardscanner.library.ViewToggleHelper

@androidx.camera.core.ExperimentalGetImage
class MainActivity : AppCompatActivity(), SdkResultCallback {

    private lateinit var binding: ActivityMainBinding
    private var showBoolean: Boolean? = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SdkActivityLauncher.setCallback(this)

        binding.scanButton.setOnClickListener {
            ViewToggleHelper.showConfirm = false
            SdkActivityLauncher.launchActivity(this, SdkActivityType.CREDIT_CARD_SCANNER)
        }

        binding.nfcButton.setOnClickListener {
            ViewToggleHelper.showIsDone = false
            SdkActivityLauncher.launchActivity(this, SdkActivityType.NFC_READER)
        }
    }

    override fun onResume() {
        super.onResume()
        if (showBoolean == true) {
            Toast.makeText(this, "showBoolean: $showBoolean", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        SdkActivityLauncher.setCallback(null)
    }

    override fun onSdkResult(result: Bundle) {
        val cardNumber = result.getString("CARD_NUMBER", "")
        val expiryDate = result.getString("EXPIRY_DATE", "")
        val cardType = result.getString("CARD_TYPE", "")
        showBoolean = result.getBoolean("CAN_EDIT", false)
        if (!cardNumber.isNullOrEmpty() && !expiryDate.isNullOrEmpty()) {
            Toast.makeText(this, "Number: $cardNumber, Date: $expiryDate", Toast.LENGTH_LONG).show()
        }
    }
}