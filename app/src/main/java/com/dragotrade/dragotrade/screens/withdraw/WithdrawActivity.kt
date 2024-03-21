package com.dragotrade.dragotrade.screens.withdraw

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.ActivityDepositBinding
import com.dragotrade.dragotrade.databinding.ActivityWithdrawBinding
import com.dragotrade.dragotrade.notification.CustomNotification
import com.dragotrade.dragotrade.screens.deposit.DepositSlipActivity
import com.dragotrade.dragotrade.utils.Constants
import com.dragotrade.dragotrade.utils.CurrentDateTime
import com.dragotrade.dragotrade.utils.LoadingBar
import com.dragotrade.dragotrade.utils.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore

class WithdrawActivity : AppCompatActivity() , View.OnClickListener{

    private lateinit var binding: ActivityWithdrawBinding
    private lateinit var preferenceManager: PreferenceManager
    private var withdrawMethod : String = ""
    private lateinit var firestore: FirebaseFirestore
    private var customNotification  = CustomNotification(this)
    private var currentDateTime  = CurrentDateTime(this)
    private var loadingBar  = LoadingBar(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWithdrawBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        init()
    }

    private fun init() {
        spinner()
        amountChange()
        preferenceManager = PreferenceManager.getInstance(this)
        setListeners()

        binding.historyButton.setOnClickListener {
            startActivity(Intent(this,WithdrawHistoryActivity::class.java))
        }
    }

    private fun setListeners() {
        binding.includeBack.backText.text = "Deposit"
        binding.includeBack.backImage.setOnClickListener(this)
        binding.withdrawButton.setOnClickListener(this)

        binding.includeBack.backImage.setOnClickListener(this)
        binding.includeDepositInput.edAmount.setOnClickListener(this)

    }

    private fun spinner() {
        val spinner = binding.includeDepositInput.selectedWallet
        val items = listOf("Choose wallet", "Binance", "OKX")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        binding.includeDepositInput.selectedWallet.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedMethod = parent?.getItemAtPosition(position).toString()

                when (selectedMethod) {
                    "Choose Withdraw Method" -> {
                        binding.withdrawButton.isEnabled = false
                        Toast.makeText(this@WithdrawActivity,"Please select withdraw method", Toast.LENGTH_SHORT).show()
                    }
                    "Binance" -> {
                        withdrawMethod = "Binance ID"
                        binding.withdrawButton.isEnabled = true
                        // binding.myCashTv.visibility = View.VISIBLE
                    } "OKX" -> {
                    withdrawMethod = "OKX"
                    binding.withdrawButton.isEnabled = true
                    //  binding.myCashTv.visibility = View.VISIBLE
                }
                    // Add more cases if needed for other withdrawal methods
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case when nothing is selected (if needed)
            }
        }
    }

    private fun amountChange() {

        binding.includeDepositInput.edAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.amount.text = s.toString()
            }
        })
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.back_image -> onBackPressedDispatcher.onBackPressed()
            R.id.withdraw_button -> withdraw()
            else -> Toast.makeText(this,"default",Toast.LENGTH_SHORT).show()
        }
    }
    private fun withdraw() {
        loadingBar.ShowDialog("please wait...")
        val email = preferenceManager.getString(Constants.KEY_EMAIL)
        val id = firestore.collection(Constants.COLLECTION_WITHDRAW).document().id
        val map = hashMapOf<String,Any>(
            Constants.KEY_BINANCE_ID to binding.includeDepositInput.binanceID.text.toString(),
            Constants.KEY_USERUID to preferenceManager.getString(Constants.KEY_USERUID),
            Constants.KEY_USERNAME to preferenceManager.getString(Constants.KEY_USERNAME),
            Constants.KEY_FULLNAME to preferenceManager.getString(Constants.KEY_FULLNAME),
            Constants.KEY_ID to id,
            Constants.KEY_AMOUNT to binding.amount.text.toString(),
            Constants.KEY_STATUS to "pending",
            Constants.KEY_EMAIL to email,
            Constants.KEY_WITHDRAW_METHOD to withdrawMethod,
            Constants.KEY_DATE to currentDateTime.getCurrentDate().toString(),
            Constants.KEY_TIME to currentDateTime.getTimeWithAmPm().toString(),
            Constants.KEY_TIMESTAMP to currentDateTime.getTimeMiles().toString(),
        )
        firestore.collection(Constants.COLLECTION_WITHDRAW).document(id)
            .set(map).addOnCompleteListener{
                if (it.isSuccessful){
                    binding.includeDepositInput.binanceID.setText("")
                    showMessage("withdraw request submitted")
                    customNotification.ShowNotification(R.drawable.logo,"New withdraw Request submitted")
                    customNotification.saveNotification(
                        preferenceManager.getString(Constants.KEY_USERUID),"New Withdraw Request"
                        ,"you have successfully submitted request to drago trade")
                    val intent = Intent(this, WithdrawSlipActivity::class.java)
                    intent.putExtra(Constants.KEY_AMOUNT,binding.amount.text.toString())
                    startActivity(intent)
                    loadingBar.HideDialog()

                }
            }.addOnFailureListener{
                Toast.makeText(this, "something went wrong!!", Toast.LENGTH_SHORT)
                    .show()
                loadingBar.HideDialog()
            }
    }
    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}