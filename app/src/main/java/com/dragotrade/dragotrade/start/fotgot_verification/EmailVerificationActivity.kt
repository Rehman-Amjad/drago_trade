package com.dragotrade.dragotrade.start.fotgot_verification

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.dragotrade.dragotrade.MainActivity
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.ActivityEmailVerificationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class EmailVerificationActivity : AppCompatActivity() {

    private lateinit var binding : ActivityEmailVerificationBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var user : FirebaseUser

    override fun onStart() {
        super.onStart()
        user = auth.currentUser!!
        if (user.isEmailVerified){
            startActivity(Intent(this,MainActivity::class.java))
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.resendButton.setOnClickListener {
            auth.currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showMessage(getString(R.string.verification_sent))
                } else {
                    showMessage(getString(R.string.verification_failed))
                }
            }
        }

        binding.openButton.setOnClickListener {
            // Open email inbox
            val intent = Intent(Intent.ACTION_VIEW)
            val data = Uri.parse("mailto:")
            intent.data = data
            startActivity(intent)
        }

    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}