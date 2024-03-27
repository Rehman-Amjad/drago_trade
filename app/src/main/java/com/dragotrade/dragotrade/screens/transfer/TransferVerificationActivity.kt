package com.dragotrade.dragotrade.screens.transfer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

    private lateinit var firestore: FirebaseFirestore
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
        Toast.makeText(this@TransferVerificationActivity, email, Toast.LENGTH_SHORT).show()
        setData()

    }

    private fun setData() {
        binding.includeTop.senderEmail.text = preferenceManager.getString(Constants.KEY_EMAIL)
        binding.includeTop.senderName.text = preferenceManager.getString(Constants.KEY_FULLNAME)
        binding.includeTop.receiverEmail.text = receiver_email
        binding.includeTop.receiverName.text = receiver_name

        binding.fullName.text = receiver_name
        binding.email.text = receiver_email
        binding.amount.text = amount
        binding.totalAmount.text = amount
    }

    private fun sendMoney() {
        val currentUserUID = firebaseAuth.currentUser?.uid // Get the current user's UID

        if (currentUserUID != null) {
            // Update the balance in the sender's wallet
            val senderWalletRef = firestore.collection("wallet").document(currentUserUID)

            firestore.runTransaction { transaction ->
                // Update sender's wallet
                val senderDoc = transaction.get(senderWalletRef)
                val senderCurrentBalance = senderDoc.getDouble("balance") ?: 0.0
                if (senderCurrentBalance < amount.toDouble()) {
                    throw IllegalStateException("Insufficient balance in sender's wallet.")
                }
                val senderUpdatedBalance = senderCurrentBalance - amount.toDouble() // Deducting amount from sender's balance
                transaction.update(senderWalletRef, "balance", senderUpdatedBalance)
            }.addOnCompleteListener { senderTransaction ->
                if (senderTransaction.isSuccessful) {
                    // If deduction from sender's wallet is successful, proceed to add amount to receiver's wallet
                    val receiverWalletRef = firestore.collection("wallet").document(receiver_userUID)

                    firestore.runTransaction { transaction ->
                        // Update receiver's wallet
                        val receiverDoc = transaction.get(receiverWalletRef)
                        val receiverCurrentBalance = receiverDoc.getDouble("balance") ?: 0.0
                        val receiverUpdatedBalance = receiverCurrentBalance + amount.toDouble() // Assuming amount is a String
                        transaction.update(receiverWalletRef, "balance", receiverUpdatedBalance)
                    }.addOnCompleteListener { receiverTransaction ->
                        if (receiverTransaction.isSuccessful) {
                            // If addition to receiver's wallet is successful, proceed to store transfer details
                            val id = firestore.collection("transfer").document().id
                            val map = hashMapOf<String, Any>(
                                Constants.KEY_EMAIL to preferenceManager.getString(Constants.KEY_EMAIL),
                                Constants.KEY_USERNAME to preferenceManager.getString(Constants.KEY_USERNAME),
                                Constants.KEY_USERUID to currentUserUID,
                                Constants.KEY_RECEIVER_ID to receiver_userUID,
                                Constants.KEY_RECEIVER_EMAIL to receiver_email,
                                Constants.KEY_RECEIVER_NAME to receiver_name,
                                Constants.KEY_AMOUNT to amount,
                                Constants.KEY_DATE to currentDateTime.getCurrentDate().toString(),
                                Constants.KEY_TIME to currentDateTime.getTimeWithAmPm().toString(),
                                Constants.KEY_TIMESTAMP to currentDateTime.getTimeMiles().toString(),
                            )

                            firestore.collection("transfer").document(id)
                                .set(map).addOnCompleteListener { transferTask ->
                                    if (transferTask.isSuccessful) {
                                        loadingBar.HideDialog()
                                        val intent = Intent(this, TransferSlipActivity::class.java)
                                        intent.putExtra(Constants.KEY_FULLNAME, receiver_name)
                                        intent.putExtra(Constants.KEY_AMOUNT, amount)
                                        intent.putExtra(Constants.KEY_DATE, currentDateTime.getCurrentDate().toString())
                                        intent.putExtra(Constants.KEY_TIME, currentDateTime.getTimeWithAmPm().toString())
                                        intent.putExtra(Constants.KEY_TIMESTAMP, currentDateTime.getTimeMiles().toString())
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        // Handle failure to store transfer details
                                        Toast.makeText(this, "Failed to store transfer details.", Toast.LENGTH_SHORT).show()
                                        loadingBar.HideDialog()
                                    }
                                }
                        } else {
                            // Handle failure to update receiver's wallet
                            Toast.makeText(this, "Failed to update receiver's wallet.", Toast.LENGTH_SHORT).show()
                            loadingBar.HideDialog()
                        }
                    }.addOnFailureListener { receiverException ->
                        // Handle any other failures in updating receiver's wallet
                        Toast.makeText(this, "Transaction failed: ${receiverException.message}", Toast.LENGTH_SHORT).show()
                        loadingBar.HideDialog()
                    }
                } else {
                    // Handle failure to update sender's wallet
                    Toast.makeText(this, "Failed to update sender's wallet.", Toast.LENGTH_SHORT).show()
                    loadingBar.HideDialog()
                }
            }.addOnFailureListener { senderException ->
                // Handle any other failures in updating sender's wallet
                Toast.makeText(this, "Transaction failed: ${senderException.message}", Toast.LENGTH_SHORT).show()
                loadingBar.HideDialog()
            }
        } else {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show()
            loadingBar.HideDialog()
        }
    }


}