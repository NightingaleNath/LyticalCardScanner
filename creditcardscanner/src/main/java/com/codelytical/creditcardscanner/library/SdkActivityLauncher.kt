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

    fun isNfcSupported(context: Context): Boolean {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        return nfcAdapter != null
    }

    @ExperimentalGetImage
    fun launchActivity(context: Context, activityType: SdkActivityType, amount: String?) {

        when (activityType) {
            SdkActivityType.NFC_READER -> {
                if (!isNfcSupported(context)) {
                    AlertDialog.Builder(context)
                        .setTitle("NFC Not Available")
                        .setMessage("NFC is not available on this device.")
                        .setPositiveButton("OK", null)
                        .show()
                    return
                }
                val intent = Intent(context, NfcReadActivity::class.java)
                intent.putExtra("amount", amount)
                context.startActivity(intent)
            }

            SdkActivityType.CREDIT_CARD_SCANNER -> {
                // Directly launch CreditCardScannerActivity without additional checks
                context.startActivity(Intent(context, CreditCardScannerActivity::class.java))
            }
        }
    }

    fun saveResult(cardNumber: String?, expiryDate: String?, cardType: String?, cardEdit: Boolean = false) {
        // Notify callback with the result
        val resultBundle = Bundle().apply {
            putString("CARD_NUMBER", cardNumber)
            putString("EXPIRY_DATE", expiryDate)
            putString("CARD_TYPE", cardType)
            putBoolean("CAN_EDIT", cardEdit)
        }
        callback?.onSdkResult(resultBundle)
    }
}


