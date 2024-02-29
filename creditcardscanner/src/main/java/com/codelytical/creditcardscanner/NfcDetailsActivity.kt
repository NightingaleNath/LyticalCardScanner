package com.codelytical.creditcardscanner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.codelytical.creditcardscanner.databinding.ActivityNfcDetailsBinding
import com.codelytical.creditcardscanner.library.SdkActivityLauncher

class NfcDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNfcDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNfcDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val cardNumber = intent.getStringExtra("CARD_NUMBER")
        val expiryDate = intent.getStringExtra("EXPIRY_DATE")
        val cardType = intent.getStringExtra("CARD_TYPE")
        val aid = intent.getStringExtra("AID")
        val application = intent.getStringExtra("APPLICATION")
        val leftPinTry = intent.getIntExtra("LEFT_PIN_TRY", 0)

        binding.txtCardNumber.text = cardNumber
        binding.txtExpiryDate.text = expiryDate
        binding.cardTypeInformation.text = cardType
        binding.applicationInformation.text = application
        binding.cardAidInformation.text = aid
        binding.pinTryInformation.text = leftPinTry.toString()

        when (cardType) {
            "VISA" -> binding.cardLogo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.visa))
            "MASTERCARD" -> binding.cardLogo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.mastercard))
            "AMEX" -> binding.cardLogo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.amex))
            "DISCOVER" -> binding.cardLogo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.discover))
            else -> {
                binding.cardLogo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.creditcard))
            }
        }


        binding.resetData.setOnClickListener {
            val detailsIntent = Intent(this, NfcReadActivity::class.java)
            startActivity(detailsIntent)

            finish()
        }

        binding.confirmButton.setOnClickListener {
            SdkActivityLauncher.saveResult(cardNumber, expiryDate, cardType)
            finish()
        }

    }
}