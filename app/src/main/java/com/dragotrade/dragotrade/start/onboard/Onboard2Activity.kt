package com.dragotrade.dragotrade.start.onboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.ActivityOnboard2Binding

class Onboard2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboard2Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboard2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.nextButton.setOnClickListener {
            startActivity(Intent(this, Onboard3Activity::class.java))
        }
    }
}