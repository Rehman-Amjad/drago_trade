package com.dragotrade.dragotrade.start

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.dragotrade.dragotrade.MainActivity
import com.dragotrade.dragotrade.databinding.ActivitySplashBinding
import com.dragotrade.dragotrade.start.onboard.Onboard1Activity
import com.dragotrade.dragotrade.start.onboard.Onboard2Activity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)


        Handler(Looper.getMainLooper()).postDelayed({
         startActivity(Intent(this,Onboard1Activity::class.java))
            finish()
        },3000)
    }

}

