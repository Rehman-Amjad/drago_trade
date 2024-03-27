package com.dragotrade.dragotrade.start.fotgot_verification

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.dragotrade.dragotrade.databinding.ActivityEmailVerifySendBinding
import com.dragotrade.dragotrade.utils.LoadingBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class EmailVerifySendActivity : AppCompatActivity() {

    private lateinit var binding : ActivityEmailVerifySendBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore : FirebaseFirestore
    private var loadingBar = LoadingBar(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailVerifySendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        binding.nextButton.setOnClickListener{

            loadingBar.ShowDialog("please wait")
            val email = binding.edEmail.text.toString()


            if (email.isNotEmpty()) {

                emailExist(email) { exists ->
                    if (exists) {
                        resetPassword(email)
                    } else {
                        loadingBar.HideDialog()
                        showToast("Email does not exist")
                    }
                }

            } else {
                loadingBar.HideDialog()
                showToast("Please enter your email address")
            }

        }

    }

    private fun emailExist(email: String, callback: (Boolean) -> Unit) {
        firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                val exists = !documents.isEmpty
                loadingBar.HideDialog()
                callback(exists)
            }
            .addOnFailureListener { exception ->
                // Handle failures
                showToast("Error checking email existence: ${exception.message}")
                callback(false) // Assuming failure means email doesn't exist
            }
    }


    private fun resetPassword(email: String) {

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Password reset email sent to $email")
                    startActivity(Intent(this, GoToGmailActivity::class.java))
                    finish()
                    // You can navigate to another activity or perform any other action here
                } else {
                    showToast("Failed to send password reset email")
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}