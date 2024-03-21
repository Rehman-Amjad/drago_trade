package com.dragotrade.dragotrade.start.onboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.ActivityOnboard4Binding
import com.dragotrade.dragotrade.start.LoginActivity

class Onboard4Activity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboard4Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboard4Binding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.nextButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}