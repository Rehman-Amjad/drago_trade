package com.dragotrade.dragotrade.start.onboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dragotrade.dragotrade.MainActivity
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.ActivityOnboard1Binding
import com.dragotrade.dragotrade.start.LoginActivity
import com.dragotrade.dragotrade.start.fotgot_verification.EmailVerificationActivity
import com.google.firebase.auth.FirebaseAuth

class Onboard1Activity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboard1Binding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onStart() {
        super.onStart()
            if (firebaseAuth.currentUser!=null){
                if (firebaseAuth.currentUser?.isEmailVerified == true){
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }else{
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }

            }else{
                startActivity(Intent(this, Onboard2Activity::class.java))
                finish()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboard1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.nextButton.setOnClickListener {
            startActivity(Intent(this,Onboard2Activity::class.java))
            finish()
        }
    }
}