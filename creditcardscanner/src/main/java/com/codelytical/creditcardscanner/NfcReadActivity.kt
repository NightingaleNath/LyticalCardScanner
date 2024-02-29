package com.codelytical.creditcardscanner

import android.app.ProgressDialog
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.ReaderCallback
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.codelytical.creditcardscanner.databinding.ActivityNfcReadBinding
import com.codelytical.creditcardscanner.nfccard.parser.EmvTemplate
import com.codelytical.creditcardscanner.provider.PcscProvider
import com.codelytical.creditcardscanner.utils.hideStatusBar
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId

class NfcReadActivity : AppCompatActivity(), ReaderCallback {

    private var progressDialog: ProgressDialog? = null

    private var mNfcAdapter: NfcAdapter? = null
    private lateinit var binding: ActivityNfcReadBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNfcReadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideStatusBar(this)

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)

        binding.closeImage.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        checkAndEnableNFC()
        if (mNfcAdapter != null) {
            val options = Bundle()
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)

            mNfcAdapter!!.enableReaderMode(
                this,
                this,
                NfcAdapter.FLAG_READER_NFC_A or
                        NfcAdapter.FLAG_READER_NFC_B or
                        NfcAdapter.FLAG_READER_NFC_F or
                        NfcAdapter.FLAG_READER_NFC_V or
                        NfcAdapter.FLAG_READER_NFC_BARCODE or
                        NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                options
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onTagDiscovered(tag: Tag?) {
        runOnUiThread {
            progressDialog = ProgressDialog(this).apply {
                setMessage("Reading NFC Tag...")
                setCancelable(false)
                show()
            }
        }

        val isoDep: IsoDep?
        try {
            isoDep = IsoDep.get(tag)
            if (isoDep != null) {
                (getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(
                    VibrationEffect.createOneShot(
                        150,
                        10
                    )
                )
            }
            isoDep.connect()
            val provider = PcscProvider()
            provider.setmTagCom(isoDep)
            val config = EmvTemplate.Config()
                .setContactLess(true)
                .setReadAllAids(true)
                .setReadTransactions(true)
                .setRemoveDefaultParsers(false)
                .setReadAt(true)
            val parser = EmvTemplate.Builder()
                .setProvider(provider)
                .setConfig(config)
                .build()
            val card = parser.readEmvCard()
            val cardNumber = card.cardNumber
            Log.d("PaymentResult: ", cardNumber)


            val expireDate = card.expireDate
            var date = LocalDate.of(1999, 12, 31)
            if (expireDate != null) {
                date = expireDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            Log.d("PaymentResultDate: ", date.toString())

            runOnUiThread {
                progressDialog?.dismiss()
               // Navigate only if I can do that here
                val firstApplication = card.applications[0]
                val aidString = firstApplication.aid.joinToString(separator = ",") { it.toUByte().toString() }
                val application = firstApplication.applicationLabel
                val leftPinTry = firstApplication.leftPinTry
                Log.d("PaymentResult: ", leftPinTry.toString())
                onNfcReadSuccessful(cardNumber, date.toString(), card.type.getName(), aidString, application, leftPinTry)
            }

            //Log.d("PaymentResultCardNumber: ", card.bic)
            Log.d("PaymentResult: ", card.type.getName())
            Log.d("PaymentResult: ", "${card.applications[0].leftPinTry} ${card.cplc}")

            try {
                isoDep.close()
            } catch (e: IOException) {
                e.printStackTrace()
                runOnUiThread { progressDialog?.dismiss() }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            runOnUiThread { progressDialog?.dismiss() }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread { progressDialog?.dismiss() }
        }
    }

    private fun checkAndEnableNFC() {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled) {
                // NFC is not enabled. Show dialog or toast prompting the user to enable NFC.
                AlertDialog.Builder(this)
                    .setTitle("NFC Disabled")
                    .setMessage("NFC is disabled. Please enable it in Settings to use NFC features.")
                    .setPositiveButton("Go to Settings") { dialog, which ->
                        startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        } else {
            // NFC is not available on this device
            Toast.makeText(this, "NFC is not available on this device.", Toast.LENGTH_LONG).show()
        }
    }

    private fun onNfcReadSuccessful(cardNumber: String, expiryDate: String, cardType: String, aid: String, application: String, leftPinTry: Int) {
        val detailsIntent = Intent(this, NfcDetailsActivity::class.java).apply {
            putExtra("CARD_NUMBER", cardNumber)
            putExtra("EXPIRY_DATE", expiryDate)
            putExtra("CARD_TYPE", cardType)
            putExtra("AID", aid)
            putExtra("APPLICATION", application)
            putExtra("LEFT_PIN_TRY", leftPinTry)
        }
        startActivity(detailsIntent)
        finish()
    }
}
