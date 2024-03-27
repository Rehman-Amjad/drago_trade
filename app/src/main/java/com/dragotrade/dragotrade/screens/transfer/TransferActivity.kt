package com.dragotrade.dragotrade.screens.transfer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.ActivityTransferBinding
import com.dragotrade.dragotrade.utils.Constants
import com.dragotrade.dragotrade.utils.LoadingBar
import com.dragotrade.dragotrade.utils.PreferenceManager
import com.dragotrade.dragotrade.utils.Validation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

class TransferActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityTransferBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var userUID : String
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private var loadingBar = LoadingBar(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding = ActivityTransferBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        preferenceManager = PreferenceManager.getInstance(this)


        setListener()
        findBalance()



    }
    private fun findBalance() {
        userUID = preferenceManager.getString(Constants.KEY_USERUID)

        firestore.collection(Constants.COLLECTION_WALLET).document(userUID)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Document exists, retrieve data
                    val walletData = documentSnapshot.data
                    val balance = walletData?.get(Constants.KEY_BALANCE).toString()

                    binding.tvBalance.text = balance

                } else {
                    // Document doesn't exist
                    Log.d("SignupActivity", "Wallet document does not exist for user: $userUID")
                }
            }
            .addOnFailureListener { e ->
                // Handle failure
                Log.e("SignupActivity", "Error retrieving wallet data: $e")
            }

    }
    private fun setListener() {
        binding.continueButton.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.continue_button ->{
                if(isValid()){
                    validates()
                }
            }
        }
    }

    private fun isValid(): Boolean {
        return true
    }

    private fun validates() {
        binding.apply {
            val senderEmail = preferenceManager.getString(Constants.KEY_EMAIL)

            if (email.text.isNullOrEmpty()) {
                showMessage(getString(R.string.email))
            } else if (!Validation.isEmailValid(email.text.toString())) {
                showMessage(getString(R.string.email_not_valid))
            } else if (email.text.toString() == senderEmail) {
                showMessage("Sender's email and receiver's email cannot be the same.")
            } else if (edAmount.text.isNullOrEmpty()) {
                showMessage("Enter amount")
            } else {
                loadingBar.ShowDialog("Please wait")
                fetchData()
            }
        }
    }

    private fun fetchData() {
        firestore.collection(Constants.COLLECTION_USER)
            .whereEqualTo(Constants.KEY_EMAIL,binding.email.text.toString())
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                @SuppressLint("NotifyDataSetChanged")
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?)
                {
                    if (error!=null)
                    {
                        Log.e("Firestore Error", "onEvent: ${error.message.toString()}" )
                        loadingBar.HideDialog()
                        return
                    }
                    if (value!!.size() <=0){
                        loadingBar.HideDialog()
                        Toast.makeText(this@TransferActivity,"Account not found", Toast.LENGTH_SHORT).show()
                    }else{
                        for (snapShot : DocumentChange in value?.documentChanges!!)
                        {
                            val email  =   snapShot.document.get(Constants.KEY_EMAIL).toString()
                            Toast.makeText(this@TransferActivity,email, Toast.LENGTH_SHORT).show()
                            if (email == binding.email.text.toString()){
                                loadingBar.HideDialog()
                                var intent = Intent(this@TransferActivity,TransferVerificationActivity::class.java)
                                intent.putExtra(Constants.KEY_EMAIL,email)
                                intent.putExtra(Constants.KEY_FULLNAME,snapShot.document.get(Constants.KEY_FULLNAME).toString())
                                intent.putExtra(Constants.KEY_USERUID,snapShot.document.get(Constants.KEY_USERUID).toString())
                                intent.putExtra(Constants.KEY_AMOUNT,binding.edAmount.text.toString())
                                startActivity(intent)
                            }else{
                                loadingBar.HideDialog()
                                Toast.makeText(this@TransferActivity,"Account not found", Toast.LENGTH_SHORT).show()
                            }


                        }
                    }

                }
            })
    }

    private fun showMessage(message: String) {

        Toast.makeText(this@TransferActivity, message, Toast.LENGTH_SHORT).show()
    }
}