package com.codelytical.creditcardscanner.library


import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.ExperimentalGetImage
import com.codelytical.creditcardscanner.CreditCardScannerActivity
import com.codelytical.creditcardscanner.NfcReadActivity

object SdkActivityLauncher {

    private var callback: SdkResultCallback? = null

    fun setCallback(listener: SdkResultCallback?) {
        this.callback = listener
    }

    @ExperimentalGetImage
    fun launchActivity(context: Context, activityType: SdkActivityType) {

        when (activityType) {
            SdkActivityType.NFC_READER -> {
                val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)
                if (nfcAdapter != null) {
                    // NFC is available, proceed to launch NfcReadActivity
                    context.startActivity(Intent(context, NfcReadActivity::class.java))
                } else {
                    // NFC is not available on this device, show an alert dialog
                    AlertDialog.Builder(context)
                        .setTitle("NFC Not Available")
                        .setMessage("NFC is not available on this device.")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }

            SdkActivityType.CREDIT_CARD_SCANNER -> {
                // Directly launch CreditCardScannerActivity without additional checks
                context.startActivity(Intent(context, CreditCardScannerActivity::class.java))
            }
        }
    }

    fun saveResult(cardNumber: String?, expiryDate: String?, cardType: String?) {
        // Notify callback with the result
        val resultBundle = Bundle().apply {
            putString("CARD_NUMBER", cardNumber)
            putString("EXPIRY_DATE", expiryDate)
            putString("CARD_TYPE", cardType)
        }
        callback?.onSdkResult(resultBundle)

    }

}


