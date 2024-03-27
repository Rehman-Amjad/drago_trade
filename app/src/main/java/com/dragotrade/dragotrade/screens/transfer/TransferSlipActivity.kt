package com.dragotrade.dragotrade.screens.transfer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dragotrade.dragotrade.MainActivity
import com.dragotrade.dragotrade.databinding.ActivityTransferSlipBinding
import com.dragotrade.dragotrade.utils.Constants
import com.dragotrade.dragotrade.utils.PreferenceManager

class TransferSlipActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransferSlipBinding
    private lateinit var preferenceManager: PreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransferSlipBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager.getInstance(this)
        getIntentValues()

        binding.returnButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun getIntentValues() {

//        val fullName = intent.getStringExtra(Constants.KEY_FULLNAME)
//        val email = intent.getStringExtra(Constants.KEY_EMAIL)
        val amount = intent.getStringExtra(Constants.KEY_AMOUNT)
        val date = intent.getStringExtra(Constants.KEY_DATE)
        val time = intent.getStringExtra(Constants.KEY_TIME)
        val mili = intent.getStringExtra(Constants.KEY_TIMESTAMP)
        putValues(amount,date,time,mili)

    }

    private fun putValues(
        amount: String?,
        date: String?,
        time: String?,
        mili: String?
    ) {

        binding.tvAmount.text = "$ "+amount
        binding.tvDate.text = date + " | "
        binding.tvTime.text = time
        binding.tvTransferID.text = mili

    }
}