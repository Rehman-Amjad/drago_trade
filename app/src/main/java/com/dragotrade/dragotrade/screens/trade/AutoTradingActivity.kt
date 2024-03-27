package com.dragotrade.dragotrade.screens.trade

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.ActivityAutoTradingBinding
import com.dragotrade.dragotrade.databinding.ActivityWithdrawBinding
import com.dragotrade.dragotrade.screens.withdraw.WithdrawSlipActivity
import com.dragotrade.dragotrade.utils.Constants

class AutoTradingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAutoTradingBinding
    private lateinit var confirm_button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAutoTradingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        topLayout()

        val greyBackgroundDrawable = resources.getDrawable(R.drawable.edit_grey_background)
        val goldBackgroundDrawable = resources.getDrawable(R.drawable.edit_gold_background)

        val clickListener = View.OnClickListener { view ->
            when (view.id) {
                R.id.tv_one -> {
                    setBackgroundAndResetOthers(binding.walletMenu.tvOne, greyBackgroundDrawable, goldBackgroundDrawable)

                }
                R.id.tv_two -> {
                    setBackgroundAndResetOthers(binding.walletMenu.tvTwo, greyBackgroundDrawable, goldBackgroundDrawable)
                    // You can set other texts similarly for other TextViews if needed
                }
                R.id.tv_three -> {
                    setBackgroundAndResetOthers(binding.walletMenu.tvThree, greyBackgroundDrawable, goldBackgroundDrawable)
                    // Similarly for other TextViews
                }
                R.id.tv_four -> {
                    setBackgroundAndResetOthers(binding.walletMenu.tvFour, greyBackgroundDrawable, goldBackgroundDrawable)
                    // Similarly for other TextViews
                }
                R.id.tv_five -> {
                    setBackgroundAndResetOthers(binding.walletMenu.tvFive, greyBackgroundDrawable, goldBackgroundDrawable)
                    // Similarly for other TextViews
                }
                R.id.tv_six -> {
                    setBackgroundAndResetOthers(binding.walletMenu.tvSix, greyBackgroundDrawable, goldBackgroundDrawable)
                    // Similarly for other TextViews
                }
            }
            // You can add more handling here if needed
        }

        click(clickListener)

        binding.confirmButton.setOnClickListener{
            val intent = Intent(this, AutoSlipActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun click(clickListener: View.OnClickListener) {
        binding.walletMenu.tvOne.setOnClickListener(clickListener)
        binding.walletMenu.tvTwo.setOnClickListener(clickListener)
        binding.walletMenu.tvThree.setOnClickListener(clickListener)
        binding.walletMenu.tvFour.setOnClickListener(clickListener)
        binding.walletMenu.tvFive.setOnClickListener(clickListener)
        binding.walletMenu.tvSix.setOnClickListener(clickListener)
    }

    private fun topLayout() {
        binding.includeBack.backText.setOnClickListener{
            onBackPressed()
            finish()
        }
        binding.includeBack.backText.setText("Auto Trading")
    }

    private fun setBackgroundAndResetOthers(clickedTextView: TextView, greyBackgroundDrawable: Drawable, goldBackgroundDrawable: Drawable) {
        // Set background of clicked TextView to gold
        clickedTextView.background = goldBackgroundDrawable

        // Reset background of all other TextViews to grey
        val textViewList = listOf(binding.walletMenu.tvOne, binding.walletMenu.tvTwo, binding.walletMenu.tvThree, binding.walletMenu.tvFour, binding.walletMenu.tvFive, binding.walletMenu.tvSix)
        for (textView in textViewList) {
            if (textView != clickedTextView) {
                textView.background = greyBackgroundDrawable
            }
        }
    }

    private fun initView() {

        //Button
        confirm_button = findViewById(R.id.confirm_button)


    }
}