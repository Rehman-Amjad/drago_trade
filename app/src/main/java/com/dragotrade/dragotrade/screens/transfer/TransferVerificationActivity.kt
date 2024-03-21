package com.dragotrade.dragotrade.screens.transfer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.ActivityTransferBinding
import com.dragotrade.dragotrade.databinding.ActivityTransferVerificationBinding
import com.dragotrade.dragotrade.utils.Constants
import com.dragotrade.dragotrade.utils.CurrentDateTime
import com.dragotrade.dragotrade.utils.LoadingBar
import com.dragotrade.dragotrade.utils.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TransferVerificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransferVerificationBinding
    private var amount: String = ""
    private var email: String = ""
    private var receiver_email: String = ""
    private var receiver_name: String = ""
    private var receiver_userUID: String = ""
    private lateinit var preferenceManager: PreferenceManager
    private var loadingBar = LoadingBar(this)
    private var currentDateTime = CurrentDateTime(this)

    private lateinit var firestore : FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransferVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        preferenceManager = PreferenceManager.getInstance(this)

        getIntentValues()

        binding.sendButton.setOnClickListener {
            loadingBar.ShowDialog("please wait...")
            sendMoney()
        }
    }



    private fun getIntentValues() {
        val intent = intent
        receiver_email = intent.getStringExtra(Constants.KEY_EMAIL).toString()
        receiver_name = intent.getStringExtra(Constants.KEY_FULLNAME).toString()
        receiver_userUID = intent.getStringExtra(Constants.KEY_USERUID).toString()
        amount = intent.getStringExtra(Constants.KEY_AMOUNT).toString()
        Toast.makeText(this@TransferVerificationActivity,email, Toast.LENGTH_SHORT).show()
        setData()

    }

    private fun setData() {
       binding.includeTop.senderEmail.text = preferenceManager.getString(Constants.KEY_EMAIL)
       binding.includeTop.receiverEmail.text = receiver_email
       binding.includeTop.receiverName.text = receiver_name

        binding.fullName.text = receiver_name
        binding.email.text = receiver_email
        binding.amount.text = amount
        binding.totalAmount.text = amount
    }

    private fun sendMoney() {
        val id = firestore.collection("transfer").document().id
        val map = hashMapOf<String, Any>(
            Constants.KEY_EMAIL to preferenceManager.getString(Constants.KEY_EMAIL),
            Constants.KEY_USERNAME to preferenceManager.getString(Constants.KEY_USERNAME),
            Constants.KEY_USERUID to preferenceManager.getString(Constants.KEY_USERUID),
            Constants.KEY_RECEIVER_ID to receiver_userUID,
            Constants.KEY_RECEIVER_EMAIL to receiver_email,
            Constants.KEY_RECEIVER_NAME to receiver_name,
            Constants.KEY_AMOUNT to amount,
            Constants.KEY_DATE to currentDateTime.getCurrentDate().toString(),
            Constants.KEY_TIME to currentDateTime.getTimeWithAmPm().toString(),
            Constants.KEY_TIMESTAMP to currentDateTime.getTimeMiles().toString(),
        )
        firestore.collection("transfer").document(id)
            .set(map).addOnCompleteListener {
                if (it.isSuccessful) {
                    loadingBar.HideDialog()
                    Toast.makeText(this, "amount saved", Toast.LENGTH_SHORT).show()
                }
            }
    }

}