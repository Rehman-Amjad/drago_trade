package com.dragotrade.dragotrade.screens.trade.highTrade

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dragotrade.dragotrade.MainActivity
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.ActivityAutoSlipBinding
import com.dragotrade.dragotrade.databinding.ActivityHighSlipBinding

class HighSlipActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHighSlipBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHighSlipBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.closeButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }
}