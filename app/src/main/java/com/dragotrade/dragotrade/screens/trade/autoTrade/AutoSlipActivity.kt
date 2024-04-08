package com.dragotrade.dragotrade.screens.trade.autoTrade

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dragotrade.dragotrade.MainActivity
import com.dragotrade.dragotrade.databinding.ActivityAutoSlipBinding

class AutoSlipActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAutoSlipBinding
    var amount : String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAutoSlipBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.closeButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }
}