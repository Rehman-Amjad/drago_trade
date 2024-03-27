package com.dragotrade.dragotrade.screens.withdraw

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.ActivityWithdrawBinding
import com.dragotrade.dragotrade.notification.CustomNotification
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
    private lateinit var userUID : String
    private var addedAmount : Double = 0.0
    private var walletUpdate : Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWithdrawBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        init()
        findBalance()
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

    private fun findBalance() {
        userUID = preferenceManager.getString(Constants.KEY_USERUID)

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

    private fun setListeners() {
        binding.includeBack.backText.text = "Withdrow"
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
                        binding.includeDepositInput.binanceID.hint = "Invalid Method"
                        binding.includeDepositInput.binanceTitle.text = "Binance ID"
                        binding.withdrawButton.isEnabled = false
                        Toast.makeText(this@WithdrawActivity,"Please select withdraw method", Toast.LENGTH_SHORT).show()
                    }
                    "Binance" -> {
                        binding.includeDepositInput.binanceID.hint = "Enter Binance ID"
                        binding.includeDepositInput.binanceTitle.text = "Binance ID"
                        withdrawMethod = "Binance ID"
                        binding.withdrawButton.isEnabled = true
                        // binding.myCashTv.visibility = View.VISIBLE
                    } "OKX" -> {
                    binding.includeDepositInput.binanceID.hint = "Enter OKX ID"
                    binding.includeDepositInput.binanceTitle.text = "OKX ID"
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
                val enteredAmount = s?.toString()?.toDoubleOrNull() ?: 0.0

                // Calculate amount after 5% deduction if entered amount is greater than or equal to 25
                 addedAmount  = if (enteredAmount >= 26) {
                    val addition = enteredAmount * 0.12
                    enteredAmount + addition
                } else {
                    enteredAmount
                }

                binding.amount.text = enteredAmount.toString()

                // Update the text with the deducted amount
                binding.tvDeductedAmount.text = if (addedAmount != enteredAmount) {
                    // Format text to display original amount and deducted amount
                    String.format("$ + 12%% tax = %.2f $", addedAmount)
                } else {
                    // Display original amount if no deduction applied
                    ""
                }
                walletUpdate = addedAmount
            }
        })
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.back_image -> onBackPressedDispatcher.onBackPressed()
            R.id.withdraw_button ->{
                loadingBar.ShowDialog("please wait...")
                if(isValid()){
                    loadingBar.HideDialog()
                    submitWithdrawRequest()
                }
            }
            else -> Toast.makeText(this,"default",Toast.LENGTH_SHORT).show()
        }
    }

    private fun isValid(): Boolean {

        val selectedMethod = binding.includeDepositInput.selectedWallet.selectedItem.toString()
        var amount = binding.includeDepositInput.edAmount.text.toString().toDoubleOrNull()
        val binanceID = binding.includeDepositInput.binanceID.text.toString()
        val balance = binding.tvBalance.text.toString().toDoubleOrNull()

        if (amount == null || amount <= 10) {
            // Prompt user to enter a valid amount (minimum 25)
            Toast.makeText(this, "The minimum amount that can be withdrawn is \$11", Toast.LENGTH_SHORT).show()
            binding.includeDepositInput.edAmount.requestFocus()
            loadingBar.HideDialog()
            return false
        }
        if (selectedMethod == "Choose wallet") {
            // Prompt user to choose a payment method
            Toast.makeText(this, "Please choose a payment method", Toast.LENGTH_SHORT).show()
            binding.includeDepositInput.selectedWallet.requestFocus()
            loadingBar.HideDialog()
            return false
        }

        if (binanceID.isBlank()) {
            // Prompt user to enter a Binance ID
            Toast.makeText(this, "Please enter your Binance/OKX ID", Toast.LENGTH_SHORT).show()
            binding.includeDepositInput.binanceID.requestFocus()
            loadingBar.HideDialog()
            return false
        }

        if (balance != null && addedAmount > balance) {
            // Prompt user that the amount to withdraw exceeds their balance
            Toast.makeText(this, "Insufficient balance for withdrawal", Toast.LENGTH_SHORT).show()
            binding.includeDepositInput.edAmount.requestFocus()
            loadingBar.HideDialog()
            return false
        }

        return true
    }

private fun submitWithdrawRequest() {

    loadingBar.ShowDialog("Submitting Withdraw Request...")

    val userUid = preferenceManager.getString(Constants.KEY_USERUID)

    // Query Firestore to check if there are any pending withdrawal requests for the user
    firestore.collection(Constants.COLLECTION_WITHDRAW)
        .whereEqualTo(Constants.KEY_USERUID, userUid)
        .whereEqualTo(Constants.KEY_STATUS, "pending")
        .get()
        .addOnSuccessListener { documents ->
            if (documents.isEmpty) {

                // No pending withdrawal requests found, proceed with submitting new request
                val withdrawAmount = binding.amount.text.toString().toDouble()

                // Update balance in Firestore
                firestore.collection(Constants.COLLECTION_WALLET).document(userUid)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val balance = documentSnapshot.getDouble(Constants.KEY_BALANCE)

                            val currentTotalWithdraw = documentSnapshot.getDouble(Constants.KEY_WALLET_TOTAL_WITHDRAW) ?: 0.0
                            val newTotalWithdraw = currentTotalWithdraw + withdrawAmount

                            val updatedBalance = balance?.minus(walletUpdate)

                            // Update balance in Firestore
                            firestore.collection(Constants.COLLECTION_WALLET).document(userUid)
                                .update(
                                    mapOf(
                                        Constants.KEY_BALANCE to updatedBalance,
                                        Constants.KEY_WALLET_TOTAL_WITHDRAW to newTotalWithdraw
                                    )
                                )
                                .addOnSuccessListener {
                                    // Balance updated successfully, proceed with submitting withdrawal request
                                    submitWithdrawRequestToFirestore()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Failed to update balance: ${e.message}", Toast.LENGTH_SHORT).show()
                                    loadingBar.HideDialog()
                                }
                        } else {
                            // Document doesn't exist
                            Log.d("WithdrawActivity", "Wallet document does not exist for user: $userUid")
                            loadingBar.HideDialog()
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle failure
                        Log.e("WithdrawActivity", "Error retrieving wallet data: $e")
                        loadingBar.HideDialog()
                    }
            } else {
                // There is a pending withdrawal request, inform the user
                showMessage("You already have a pending withdrawal request.")
                loadingBar.HideDialog()
            }
        }
        .addOnFailureListener { exception ->
            Toast.makeText(this, "Failed to check withdrawal requests: ${exception.message}", Toast.LENGTH_SHORT).show()
            loadingBar.HideDialog()
        }
}

    private fun submitWithdrawRequestToFirestore() {
        // Withdrawal request data
        val email = preferenceManager.getString(Constants.KEY_EMAIL)
        val id = firestore.collection(Constants.COLLECTION_WITHDRAW).document().id
        val withdrawAmount = binding.amount.text.toString()
        val binanceID = binding.includeDepositInput.binanceID.text.toString()

        // Withdrawal request details
        val withdrawRequest = hashMapOf(
            Constants.KEY_BINANCE_ID to binanceID,
            Constants.KEY_USERUID to preferenceManager.getString(Constants.KEY_USERUID),
            Constants.KEY_USERNAME to preferenceManager.getString(Constants.KEY_USERNAME),
            Constants.KEY_FULLNAME to preferenceManager.getString(Constants.KEY_FULLNAME),
            Constants.KEY_ID to id,
            Constants.KEY_AMOUNT to withdrawAmount,
            Constants.KEY_STATUS to "pending",
            Constants.KEY_EMAIL to email,
            Constants.KEY_WITHDRAW_METHOD to withdrawMethod,
            Constants.KEY_DATE to currentDateTime.getCurrentDate(),
            Constants.KEY_TIME to currentDateTime.getTimeWithAmPm(),
            Constants.KEY_TIMESTAMP to currentDateTime.getTimeMiles(),
        )

        // Submit withdrawal request to Firestore
        firestore.collection(Constants.COLLECTION_WITHDRAW).document(id)
            .set(withdrawRequest)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Withdrawal request submitted successfully
                    showMessage("Withdraw request submitted")
                    customNotification.ShowNotification(R.drawable.logo, "New withdraw Request submitted")
                    customNotification.saveNotification(
                        preferenceManager.getString(Constants.KEY_USERUID), "New Withdraw Request",
                        "You have successfully submitted request to Dragotrade"
                    )

                    // Start WithdrawSlipActivity
                    val intent = Intent(this, WithdrawSlipActivity::class.java)
                    intent.putExtra(Constants.KEY_AMOUNT, withdrawAmount)
                    startActivity(intent)
                    loadingBar.HideDialog()
                    finish()
                } else {
                    // Failed to submit withdrawal request
                    Toast.makeText(this, "Failed to submit withdraw request", Toast.LENGTH_SHORT).show()
                    loadingBar.HideDialog()
                }
            }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}