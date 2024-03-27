package com.dragotrade.dragotrade.bottomFragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.TestActivity
import com.dragotrade.dragotrade.databinding.FragmentHomeBinding
import com.dragotrade.dragotrade.screens.deposit.DepositActivity
import com.dragotrade.dragotrade.screens.deposit.DepositHistoryActivity
import com.dragotrade.dragotrade.notification.NotificationActivity
import com.dragotrade.dragotrade.screens.trade.AutoTradingActivity
import com.dragotrade.dragotrade.screens.trade.HighTradingActivity
import com.dragotrade.dragotrade.screens.transfer.TransferActivity
import com.dragotrade.dragotrade.screens.wallet.WalletActivity
import com.dragotrade.dragotrade.screens.withdraw.WithdrawActivity
import com.dragotrade.dragotrade.utils.Constants
import com.dragotrade.dragotrade.utils.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class HomeFragment : Fragment(),View.OnClickListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var userUID : String

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =  FragmentHomeBinding.inflate(layoutInflater, container, false)

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
    private fun findBalance() {
        binding.tvBalance.text = "please wait"

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
        binding.notificationImage.setOnClickListener(this)
        binding.depositButton.setOnClickListener(this)
        binding.includeMenu.llDeposit.setOnClickListener(this)
        binding.includeMenu.llHistory.setOnClickListener(this)
        binding.includeMenu.llWithdraw.setOnClickListener(this)
        binding.includeMenu.llTransfer.setOnClickListener(this)
        binding.includeMenu.llWallet.setOnClickListener(this)
        binding.includeMenu.llAuto.setOnClickListener(this)
        binding.includeMenu.llHigh.setOnClickListener(this)

        binding.autoButton.setOnClickListener(this)
        binding.highButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.notification_image -> startActivity(Intent(requireContext(),NotificationActivity::class.java))
            R.id.ll_deposit -> startActivity(Intent(requireContext(), DepositActivity::class.java))
            R.id.deposit_button -> startActivity(Intent(requireContext(), DepositActivity::class.java))
            R.id.ll_history -> startActivity(Intent(requireContext(), DepositHistoryActivity::class.java))
            R.id.ll_withdraw -> startActivity(Intent(requireContext(), WithdrawActivity::class.java))
            R.id.ll_transfer -> startActivity(Intent(requireContext(), TransferActivity::class.java))
            R.id.ll_wallet -> startActivity(Intent(requireContext(), WalletActivity::class.java))

            R.id.auto_button -> startActivity(Intent(requireContext(), AutoTradingActivity::class.java))
            R.id.high_button -> startActivity(Intent(requireContext(), HighTradingActivity::class.java))

            R.id.ll_auto -> startActivity(Intent(requireContext(), TestActivity::class.java))
            R.id.ll_high -> startActivity(Intent(requireContext(), HighTradingActivity::class.java))

            R.id.img_reload -> findBalance()
        }
    }

}