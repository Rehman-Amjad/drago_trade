package com.dragotrade.dragotrade.start.fotgot_verification

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.ActivityEmailVerificationBinding
import com.dragotrade.dragotrade.databinding.ActivityGoToGmailBinding

class GoToGmailActivity : AppCompatActivity() {

    private lateinit var binding : ActivityGoToGmailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoToGmailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.openButton.setOnClickListener {
            // Open email inbox
//            val intent = Intent(Intent.ACTION_VIEW)
//            val data = Uri.parse("mailto:")
//            intent.data = data
//            startActivity(intent)
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_APP_EMAIL)
            startActivity(Intent.createChooser(intent, "Email"))
        }
    }
}