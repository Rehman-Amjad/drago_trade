package com.dragotrade.dragotrade.bottomFragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.FragmentHomeBinding
import com.dragotrade.dragotrade.screens.deposit.DepositActivity
import com.dragotrade.dragotrade.screens.deposit.DepositHistoryActivity
import com.dragotrade.dragotrade.notification.NotificationActivity
import com.dragotrade.dragotrade.screens.chat.ChatActivity
import com.dragotrade.dragotrade.screens.transfer.TransferActivity
import com.dragotrade.dragotrade.screens.wallet.WalletActivity
import com.dragotrade.dragotrade.screens.withdraw.WithdrawActivity


class HomeFragment : Fragment(),View.OnClickListener {

    private lateinit var binding: FragmentHomeBinding

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

        inits()

    }

    private fun inits() {
        setListener()
        binding.apply {
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
        }
    }

}