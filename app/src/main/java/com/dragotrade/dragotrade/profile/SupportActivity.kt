package com.dragotrade.dragotrade.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.ActivitySupportBinding

class SupportActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySupportBinding

    private var ques1 = false
    private var ques2 = false
    private var ques3 = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backImage.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

       binding.ques1.setOnClickListener {
            if (ques1){
                ques1 = false
                binding.ans1.visibility = View.GONE
            }else{
                ques1 = true
                binding.ans1.visibility = View.VISIBLE
            }
       }

        binding.ques2.setOnClickListener {
            if (ques2){
                ques2 = false
                binding.ans2.visibility = View.GONE
            }else{
                ques2 = true
                binding.ans2.visibility = View.VISIBLE
            }
        }

        binding.ques3.setOnClickListener {
            if (ques3){
                ques3 = false
                binding.ans3.visibility = View.GONE
            }else{
                ques3 = true
                binding.ans3.visibility = View.VISIBLE
            }
        }

    }
}