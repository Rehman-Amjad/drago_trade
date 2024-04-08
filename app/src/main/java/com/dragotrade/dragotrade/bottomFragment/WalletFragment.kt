package com.dragotrade.dragotrade.bottomFragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.FragmentWalletBinding
import com.dragotrade.dragotrade.screens.deposit.DepositActivity
import com.dragotrade.dragotrade.screens.deposit.DepositHistoryActivity
import com.dragotrade.dragotrade.screens.transfer.TransferActivity
import com.dragotrade.dragotrade.screens.transfer.TransferHistoryActivity
import com.dragotrade.dragotrade.screens.withdraw.WithdrawActivity
import com.dragotrade.dragotrade.screens.withdraw.WithdrawHistoryActivity
import com.dragotrade.dragotrade.utils.Constants
import com.dragotrade.dragotrade.utils.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class WalletFragment : Fragment() ,View.OnClickListener{

    private lateinit var binding: FragmentWalletBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var userUID : String
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentWalletBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        preferenceManager = PreferenceManager.getInstance(requireContext())
        userUID = firebaseAuth.currentUser?.uid.toString()

        inits()
        findBalance()
    }

    private fun inits() {
        setListener()
        binding.apply {
        }
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
        when(v?.id){
            R.id.ll_sendPayment -> startActivity(Intent(requireContext(),TransferActivity::class.java))
            R.id.ll_transferHistory -> startActivity(Intent(requireContext(),TransferHistoryActivity::class.java))
            R.id.ll_deposit -> startActivity(Intent(requireContext(),DepositActivity::class.java))
            R.id.add_image -> startActivity(Intent(requireContext(),DepositActivity::class.java))
            R.id.ll_depositHistory -> startActivity(Intent(requireContext(),DepositHistoryActivity::class.java))
            R.id.ll_withdraw -> startActivity(Intent(requireContext(),WithdrawActivity::class.java))
            R.id.ll_withdrawHistory -> startActivity(Intent(requireContext(),WithdrawHistoryActivity::class.java))
        }
    }

    private fun findBalance() {
        firestore.collection(Constants.COLLECTION_WALLET).document(userUID)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Document exists, retrieve data
                    val walletData = documentSnapshot.data
                    val balance = walletData?.get(Constants.KEY_BALANCE)?.toString()?.toDoubleOrNull()

                    binding.walletBalance.tvBalance.text = String.format("%.2f", balance)

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

}