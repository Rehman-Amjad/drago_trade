package com.dragotrade.dragotrade.screens.withdraw

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dragotrade.dragotrade.MainActivity
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.ActivityDepositSlipBinding
import com.dragotrade.dragotrade.databinding.ActivityWithdrawSlipBinding
import com.dragotrade.dragotrade.utils.Constants

class WithdrawSlipActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWithdrawSlipBinding
    var amount : String? = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWithdrawSlipBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        amount =  intent.getStringExtra(Constants.KEY_AMOUNT)

        binding.amount.text = "$$amount"


        binding.closeButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }
}