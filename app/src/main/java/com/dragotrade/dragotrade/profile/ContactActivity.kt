package com.dragotrade.dragotrade.profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.ActivityContactBinding
import com.dragotrade.dragotrade.databinding.ActivityWalletBinding
import com.dragotrade.dragotrade.screens.chat.ChatActivity
import com.dragotrade.dragotrade.screens.transfer.TransferActivity
import com.dragotrade.dragotrade.utils.PreferenceManager

class ContactActivity : AppCompatActivity() {

    private lateinit var binding : ActivityContactBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chatButton.setOnClickListener{
            startActivity(Intent(this, ChatActivity::class.java))
        }
    }
}