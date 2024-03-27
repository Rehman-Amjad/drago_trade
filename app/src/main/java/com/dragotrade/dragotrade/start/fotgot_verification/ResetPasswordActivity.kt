package com.dragotrade.dragotrade.start.fotgot_verification

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.ActivityEmailVerifySendBinding
import com.dragotrade.dragotrade.databinding.ActivityResetPasswordBinding
import com.dragotrade.dragotrade.start.LoginActivity
import com.dragotrade.dragotrade.utils.LoadingBar
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding : ActivityResetPasswordBinding
    private lateinit var email : String
    private lateinit var firebaseAuth: FirebaseAuth
    private var loadingBar = LoadingBar(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initValue()
        getIntentValue()


        binding.restButton.setOnClickListener {
            loadingBar.ShowDialog("please wait...")

            val newPassword = binding.edPassword.text.toString()
            val confirmNewPassword = binding.edConfirmPassword.text.toString()

            if (email.isNotEmpty() && newPassword.isNotEmpty() && confirmNewPassword.isNotEmpty()) {
                sendPasswordResetEmail(email, newPassword, confirmNewPassword)
            } else {
                loadingBar.HideDialog()
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun initValue() {
        firebaseAuth = FirebaseAuth.getInstance()
    }


private fun sendPasswordResetEmail(email: String, newPassword: String, confirmNewPassword: String) {
    if (newPassword != confirmNewPassword) {
        Toast.makeText(this, "New password and confirm password do not match", Toast.LENGTH_SHORT).show()
        return
    }

    firebaseAuth.sendPasswordResetEmail(email)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Password reset email sent successfully, now update the password
                firebaseAuth.confirmPasswordReset("code_from_email", newPassword)
                    .addOnCompleteListener { newPasswordTask ->
                        if (newPasswordTask.isSuccessful) {
                            Toast.makeText(
                                this,
                                "Password successfully reset",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Failed to reset password. ${newPasswordTask.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(
                    this,
                    "Failed to send password reset email. ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
}



    private fun getIntentValue() {
        email  = intent.getStringExtra("EMAIL").toString()
    }
}