package com.dragotrade.dragotrade.screens.wallet

import android.content.Intent
import android.os.Bundle
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
import com.dragotrade.dragotrade.screens.withdraw.WithdrawActivity
import com.dragotrade.dragotrade.screens.withdraw.WithdrawHistoryActivity

class WalletActivity : AppCompatActivity(), View.OnClickListener  {

    private lateinit var binding : ActivityWalletBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)

        inits()

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
            R.id.ll_transferHistory -> startActivity(Intent(this,TransferActivity::class.java))
            R.id.ll_deposit -> startActivity(Intent(this,DepositActivity::class.java))
            R.id.add_image -> startActivity(Intent(this,DepositActivity::class.java))
            R.id.ll_depositHistory -> startActivity(Intent(this,DepositHistoryActivity::class.java))
            R.id.ll_withdraw -> startActivity(Intent(this,WithdrawActivity::class.java))
            R.id.ll_withdrawHistory -> startActivity(Intent(this,WithdrawHistoryActivity::class.java))
        }
    }
}