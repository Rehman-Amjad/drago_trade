package com.dragotrade.dragotrade.start.onboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.ActivityOnboard3Binding

class Onboard3Activity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboard3Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboard3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.nextButton.setOnClickListener {
            startActivity(Intent(this, Onboard4Activity::class.java))
        }
    }
}