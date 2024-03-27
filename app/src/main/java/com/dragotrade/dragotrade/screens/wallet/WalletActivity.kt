package com.dragotrade.dragotrade.screens.wallet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.ActivityWalletBinding
import com.dragotrade.dragotrade.screens.deposit.DepositActivity
import com.dragotrade.dragotrade.screens.deposit.DepositHistoryActivity
import com.dragotrade.dragotrade.screens.transfer.TransferActivity
import com.dragotrade.dragotrade.screens.transfer.TransferHistoryActivity
import com.dragotrade.dragotrade.screens.withdraw.WithdrawActivity
import com.dragotrade.dragotrade.screens.withdraw.WithdrawHistoryActivity
import com.dragotrade.dragotrade.utils.Constants
import com.dragotrade.dragotrade.utils.LoadingBar
import com.dragotrade.dragotrade.utils.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WalletActivity : AppCompatActivity(), View.OnClickListener  {

    private lateinit var binding : ActivityWalletBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var userUID : String
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private var loadingBar = LoadingBar(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        preferenceManager = PreferenceManager.getInstance(this)

        inits()
        findBalance()

    }

    private fun inits() {
        setListener()
    }

    private fun setListener() {
        binding.walletMenu.llSendPayment.setOnClickListener(this)
        binding.walletMenu.llTransferHistory.setOnClickListener(this)
        binding.walletMenu.llDeposit.setOnClickListener(this)
        binding.walletMenu.llDepositHistory.setOnClickListener(this)
        binding.walletMenu.llWithdraw.setOnClickListener(this)
        binding.walletMenu.llWithdrawHistory.setOnClickListener(this)
        binding.walletBalance.addImage.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ll_sendPayment -> startActivity(Intent(this,TransferActivity::class.java))
            R.id.ll_transferHistory -> startActivity(Intent(this,TransferHistoryActivity::class.java))
            R.id.ll_deposit -> startActivity(Intent(this,DepositActivity::class.java))
            R.id.add_image -> startActivity(Intent(this,DepositActivity::class.java))
            R.id.ll_depositHistory -> startActivity(Intent(this,DepositHistoryActivity::class.java))
            R.id.ll_withdraw -> startActivity(Intent(this,WithdrawActivity::class.java))
            R.id.ll_withdrawHistory -> startActivity(Intent(this,WithdrawHistoryActivity::class.java))
        }
    }

    private fun findBalance() {
        loadingBar.ShowDialog("Please Wait...")
        userUID = preferenceManager.getString(Constants.KEY_USERUID)

        firestore.collection(Constants.COLLECTION_WALLET).document(userUID)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Document exists, retrieve data
                    val walletData = documentSnapshot.data
                    val balance = walletData?.get(Constants.KEY_BALANCE).toString()

                    loadingBar.HideDialog()
                    binding.walletBalance.tvBalance.text = balance

                } else {
                    loadingBar.HideDialog()
                    // Document doesn't exist
                    Log.d("SignupActivity", "Wallet document does not exist for user: $userUID")
                }
            }
            .addOnFailureListener { e ->
                // Handle failure
                loadingBar.HideDialog()
                Log.e("SignupActivity", "Error retrieving wallet data: $e")
            }

    }
}