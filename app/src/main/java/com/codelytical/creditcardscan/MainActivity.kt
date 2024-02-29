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

@androidx.camera.core.ExperimentalGetImage
class MainActivity : AppCompatActivity(), SdkResultCallback {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SdkActivityLauncher.setCallback(this)

        binding.scanButton.setOnClickListener {
            SdkActivityLauncher.launchActivity(this, SdkActivityType.CREDIT_CARD_SCANNER)
        }

        binding.nfcButton.setOnClickListener {
            SdkActivityLauncher.launchActivity(this, SdkActivityType.NFC_READER)
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
        if (!cardNumber.isNullOrEmpty() && !expiryDate.isNullOrEmpty()) {
            Toast.makeText(this, "Card Number: $cardNumber, Expiry Date: $expiryDate", Toast.LENGTH_LONG).show()
        }
    }
}