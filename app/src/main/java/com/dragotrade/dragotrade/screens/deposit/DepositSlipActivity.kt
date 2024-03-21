package com.dragotrade.dragotrade.screens.deposit

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dragotrade.dragotrade.MainActivity
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.ActivityDepositSlipBinding


import com.dragotrade.dragotrade.utils.Constants

class DepositSlipActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDepositSlipBinding
    var amount : String? = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDepositSlipBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        amount =  intent.getStringExtra(Constants.KEY_AMOUNT)

        binding.subTotal.text   = "$$amount"
        binding.total.text   = "$$amount"
        binding.amount.text = "$$amount"


        binding.thankYouButton.setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }

    }
}