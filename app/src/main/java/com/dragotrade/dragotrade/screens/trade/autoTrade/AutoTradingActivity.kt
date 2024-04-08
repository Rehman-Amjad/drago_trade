package com.dragotrade.dragotrade.screens.trade.autoTrade

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.ActivityAutoTradingBinding
import com.dragotrade.dragotrade.utils.CongratulationDialog
import com.dragotrade.dragotrade.utils.Constants
import com.dragotrade.dragotrade.utils.CurrentDateTime
import com.dragotrade.dragotrade.utils.LoadingBar
import com.dragotrade.dragotrade.utils.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AutoTradingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAutoTradingBinding
    private var days : Double = 1.0
    private var amount : Double = 0.0
    private var percentage : Double = 2.0
    private var profit : Double = 0.0

    private lateinit var userUID : String
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    private var loadingBar  = LoadingBar(this)
    private var congratulationDialog = CongratulationDialog(this)
    private var currentDateTime  = CurrentDateTime(this)
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAutoTradingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        userUID = firebaseAuth.currentUser?.uid.toString()
        preferenceManager = PreferenceManager.getInstance(this)

        topLayout()
        findBalance()
        getRequestDate()

        binding.tvDay.text = "1 Day"
        binding.tvPercentage.text= "2.0 %"

        val greyBackgroundDrawable = resources.getDrawable(R.drawable.edit_grey_background)
        val goldBackgroundDrawable = resources.getDrawable(R.drawable.edit_gold_background)

        val clickListener = View.OnClickListener { view ->
            when (view.id) {
                R.id.tv_one -> {
                    setBackgroundAndResetOthers(binding.walletMenu.tvOne, greyBackgroundDrawable, goldBackgroundDrawable)
                    days = 1.0
                    percentage = 2.0
                    binding.tvDay.text = "1 Day"
                    binding.tvPercentage.text= "2.0 %"
                    binding.edAmount.text.clear()
                }
                R.id.tv_two -> {
                    setBackgroundAndResetOthers(binding.walletMenu.tvTwo, greyBackgroundDrawable, goldBackgroundDrawable)
                    days = 3.0
                    percentage = 2.5
                    binding.tvPercentage.text= "2.5 %"
                    binding.tvDay.text= "3 Days"
                    binding.edAmount.text.clear()
                }
                R.id.tv_three -> {
                    setBackgroundAndResetOthers(binding.walletMenu.tvThree, greyBackgroundDrawable, goldBackgroundDrawable)
                    days = 7.0
                    percentage = 3.0
                    binding.tvPercentage.text= "3.0 %"
                    binding.tvDay.text= "7 Days"
                    binding.edAmount.text.clear()
                }
                R.id.tv_four -> {
                    setBackgroundAndResetOthers(binding.walletMenu.tvFour, greyBackgroundDrawable, goldBackgroundDrawable)
                    days = 15.0
                    percentage = 3.75
                    binding.tvPercentage.text= "3.75 %"
                    binding.tvDay.text= "15 Days"
                    binding.edAmount.text.clear()
                }
                R.id.tv_five -> {
                    setBackgroundAndResetOthers(binding.walletMenu.tvFive, greyBackgroundDrawable, goldBackgroundDrawable)
                    days = 30.0
                    percentage = 4.75
                    binding.tvPercentage.text= "4.75 %"
                    binding.tvDay.text= "30 Days"
                    binding.edAmount.text.clear()
                }
                R.id.tv_six -> {
                    setBackgroundAndResetOthers(binding.walletMenu.tvSix, greyBackgroundDrawable, goldBackgroundDrawable)
                    days = 45.0
                    percentage = 5.75
                    binding.tvPercentage.text= "5.75 %"
                    binding.tvDay.text= "45 Days"
                    binding.edAmount.text.clear()
                }
                R.id.tv_seven -> {
                    setBackgroundAndResetOthers(binding.walletMenu.tvSeven, greyBackgroundDrawable, goldBackgroundDrawable)
                    days = 60.0
                    percentage = 6.75
                    binding.tvPercentage.text= "6.75 %"
                    binding.tvDay.text= "60 Days"
                    binding.edAmount.text.clear()
                }
            }
            // You can add more handling here if needed
        }

        click(clickListener)

        binding.edAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Calculate profit based on the entered amount and selected days
                val enteredAmount = s.toString().toDoubleOrNull() ?: 0.0

                profit = enteredAmount * percentage / 100 * days

                binding.tvProfit.text = "$" + String.format("%.2f", profit)
                binding.tvAmount.text = "$" + String.format("%.2f", enteredAmount)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.confirmButton.setOnClickListener{
            loadingBar.ShowDialog("please wait...")
            if(isValid()){
                checkPendingRequests()
            }
        }

        binding.tvHistory.setOnClickListener{
            val intent = Intent(this, TradeHistoryActivity::class.java)
            startActivity(intent)
        }

    }


    private fun click(clickListener: View.OnClickListener) {
        binding.walletMenu.tvOne.setOnClickListener(clickListener)
        binding.walletMenu.tvTwo.setOnClickListener(clickListener)
        binding.walletMenu.tvThree.setOnClickListener(clickListener)
        binding.walletMenu.tvFour.setOnClickListener(clickListener)
        binding.walletMenu.tvFive.setOnClickListener(clickListener)
        binding.walletMenu.tvSix.setOnClickListener(clickListener)
        binding.walletMenu.tvSeven.setOnClickListener(clickListener)
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
        val textViewList = listOf(binding.walletMenu.tvOne,
            binding.walletMenu.tvTwo,
            binding.walletMenu.tvThree,
            binding.walletMenu.tvFour,
            binding.walletMenu.tvFive,
            binding.walletMenu.tvSix,
            binding.walletMenu.tvSeven)

        for (textView in textViewList) {
            if (textView != clickedTextView) {
                textView.background = greyBackgroundDrawable
            }
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
                    val balance = walletData?.get(Constants.KEY_BALANCE)?.toString()?.toDoubleOrNull()

                    binding.tvBalance.text = String.format("%.2f", balance)

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
    private fun isValid(): Boolean {
        val enteredAmount = binding.edAmount.text.toString().toDoubleOrNull() ?: 0.0
        val userBalance = binding.tvBalance.text.toString().toDoubleOrNull() ?: 0.0

        if(binding.edAmount.text.isEmpty()){
            loadingBar.HideDialog()
            Toast.makeText(this,"Amount is required.",Toast.LENGTH_SHORT).show()
            return false
        }

        if (enteredAmount > userBalance) {
            loadingBar.HideDialog()
            Toast.makeText(this, "Insufficient balance.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (days.toInt() == 1 && enteredAmount > 100) {
            loadingBar.HideDialog()
            Toast.makeText(this, "Maximum amount for 1 day investment is $100.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (days.toInt() == 3 && enteredAmount > 200) {
            loadingBar.HideDialog()
            Toast.makeText(this, "Maximum amount for 3 day investment is $200.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun checkPendingRequests() {
        firestore.collection("autoTrading")
            .whereEqualTo(Constants.KEY_STATUS, "pending")
            .whereEqualTo(Constants.KEY_USERUID, userUID)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    loadingBar.HideDialog()
                    Toast.makeText(this, "There are pending requests.", Toast.LENGTH_SHORT).show()
                } else {
                    submitRequestToFirestore()
                }
            }
            .addOnFailureListener { e ->
                // Handle failure
                loadingBar.HideDialog()
                Log.e("AutoTradingActivity", "Error checking pending requests: $e")
                Toast.makeText(this, "Failed to check pending requests.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun submitRequestToFirestore() {
        val enteredAmount = binding.edAmount.text.toString().toDoubleOrNull() ?: 0.0

        // Add document for amount spent, time, date, and userUid
        val amountSpentData = hashMapOf(
            Constants.KEY_USERUID to preferenceManager.getString(Constants.KEY_USERUID),
            Constants.KEY_USERNAME to preferenceManager.getString(Constants.KEY_USERNAME),
            Constants.KEY_FULLNAME to preferenceManager.getString(Constants.KEY_FULLNAME),
            "amountSpent" to enteredAmount,
            "type" to "bought",
            Constants.KEY_DATE to currentDateTime.getCurrentDate(),
            Constants.KEY_TIME to currentDateTime.getTimeWithAmPm(),
            Constants.KEY_TIMESTAMP to currentDateTime.getTimeMiles(),
        )

        firestore.collection("autoTrading").document().set(amountSpentData)
            .addOnSuccessListener {

                // Calculate daily profit
                val dailyProfit = (profit + enteredAmount) / days

                val id = firestore.collection("autoTrading").document().id
                // Trade request details
                val tradeRequest = hashMapOf(
                    Constants.KEY_USERUID to preferenceManager.getString(Constants.KEY_USERUID),
                    Constants.KEY_USERNAME to preferenceManager.getString(Constants.KEY_USERNAME),
                    Constants.KEY_FULLNAME to preferenceManager.getString(Constants.KEY_FULLNAME),
                    Constants.KEY_ID to id,
                    Constants.KEY_STATUS to "pending",
                    "type" to "Active",
                    Constants.KEY_DATE to currentDateTime.getCurrentDate(),
                    Constants.KEY_TIME to currentDateTime.getTimeWithAmPm(),
                    Constants.KEY_TIMESTAMP to currentDateTime.getTimeMiles(),
                    "days" to days,
                    "percentage" to percentage,
                    "profit" to profit + enteredAmount,
                    "profitAmount" to profit,
                    "dailyProfit" to dailyProfit,
                    "amountSpent" to enteredAmount
                )

                // Submit main trade request to Firestore
                firestore.collection("autoTrading").document(id)
                    .set(tradeRequest)
                    .addOnSuccessListener {
                        // Request sent successfully
                        Toast.makeText(this, "Trade request submitted", Toast.LENGTH_SHORT).show()

                        // Deduct the amount from the user's balance
                        val userBalance = binding.tvBalance.text.toString().toDoubleOrNull() ?: 0.0
                        firestore.collection(Constants.COLLECTION_WALLET).document(userUID)
                            .update(Constants.KEY_BALANCE, userBalance - enteredAmount)
                            .addOnSuccessListener {
                                // Balance updated successfully
                                Toast.makeText(this, "Balance Updated", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this, AutoSlipActivity::class.java)
                                startActivity(intent)
                                finish()

                                loadingBar.HideDialog()
                            }
                            .addOnFailureListener { e ->
                                // Failed to update balance
                                Log.e("AutoTradingActivity", "Error updating balance: $e")
                                Toast.makeText(this, "Failed to update balance", Toast.LENGTH_SHORT).show()
                                loadingBar.HideDialog()
                            }
                    }
                    .addOnFailureListener { e ->
                        // Failed to submit trade request
                        Log.e("AutoTradingActivity", "Error submitting trade request: $e")
                        Toast.makeText(this, "Failed to submit trade request", Toast.LENGTH_SHORT).show()
                        loadingBar.HideDialog()
                    }
            }
            .addOnFailureListener { e ->
                // Failed to add amount spent document
                Log.e("AutoTradingActivity", "Error adding amount spent document: $e")
                Toast.makeText(this, "Failed to add document", Toast.LENGTH_SHORT).show()
                loadingBar.HideDialog()
            }
    }

//    private fun getRequestDate() {
//        firestore.collection("autoTrading")
//            .whereEqualTo(Constants.KEY_USERUID, userUID)
//            .whereEqualTo(Constants.KEY_STATUS, "pending")
//            .get()
//            .addOnSuccessListener { documents ->
//                for (document in documents) {
//                    val daysFromDocument = document.getDouble("days")
//                    val profitAmount = document.getDouble("profit")
//
//                    val tradeDateString = document.getString(Constants.KEY_DATE)
//                    val dateFormat = SimpleDateFormat("dd/MMM/yyyy", Locale.getDefault())
//                    val tradeDate = dateFormat.parse(tradeDateString)
//
//                    if (tradeDate != null && daysFromDocument != null && profitAmount != null) {
//                        val currentDate = Date()
//                        val daysDifference = currentDateTime.getDaysDifference(currentDate, tradeDate)
//
//                        if (daysDifference != null) {
//                            if (daysDifference >= daysFromDocument) {
//                                updateBalanceAndCompleteRequest(document, profitAmount)
//                            } else {
//                                val remainingDays = daysFromDocument - daysDifference
//                                binding.tvPending.text = "Remaining Days: ${remainingDays.toInt()}"
//                            }
//                        }
//                    }
//                }
//            }
//            .addOnFailureListener { e ->
//                // Handle failure
//                Log.e("AutoTradingActivity", "Error checking pending request date: $e")
//                Toast.makeText(this, "Failed to check pending request date.", Toast.LENGTH_SHORT).show()
//            }
//    }

    private fun getRequestDate() {
        firestore.collection("autoTrading")
            .whereEqualTo(Constants.KEY_USERUID, userUID)
            .whereEqualTo(Constants.KEY_STATUS, "pending")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val daysFromDocument = document.getDouble("days")
                    val profitAmount = document.getDouble("profit")

                    val tradeDateString = document.getString(Constants.KEY_DATE)
                    val dateFormat = SimpleDateFormat("dd/MMM/yyyy", Locale.getDefault())
                    val tradeDate = dateFormat.parse(tradeDateString)

                    if (tradeDate != null && daysFromDocument != null && profitAmount != null) {
                        val currentDate = Date()
                        val timeDifference = currentDateTime.getTimeDifference(currentDate, tradeDate)

                        val remainingDays = timeDifference.days
                        val remainingHours = timeDifference.hours
                        val remainingMinutes = timeDifference.minutes
                        val remainingSeconds = timeDifference.seconds

                        val remainingTime = "Remaining Time: ${remainingDays}d ${remainingHours}h ${remainingMinutes}m ${remainingSeconds}s"
                        binding.tvPending.text = remainingTime

                        // Start countdown timer
                        val endTimeInMillis = tradeDate.time + daysFromDocument * 24 * 60 * 60 * 1000 // Calculate end time in milliseconds
                        startCountDownTimer(endTimeInMillis,document,profitAmount)
                    }
                }
            }
            .addOnFailureListener { e ->
                // Handle failure
                Log.e("AutoTradingActivity", "Error checking pending request date: $e")
                Toast.makeText(this, "Failed to check pending request date.", Toast.LENGTH_SHORT).show()
            }
    }




    private fun updateBalanceAndCompleteRequest(document: DocumentSnapshot, profitAmount: Double) {
        // Update user balance in the wallet by adding the profit amount
        firestore.collection(Constants.COLLECTION_WALLET).document(userUID)
            .get()
            .addOnSuccessListener { walletDoc ->
                val currentBalance = walletDoc.getDouble(Constants.KEY_BALANCE) ?: 0.0
                val newBalance = currentBalance + profitAmount
                // Update balance in the wallet
                firestore.collection(Constants.COLLECTION_WALLET).document(userUID)
                    .update(Constants.KEY_BALANCE, newBalance)
                    .addOnSuccessListener {
                        // Update the document status to "completed"
                        completePendingRequest(document)
                    }
                    .addOnFailureListener { e ->
                        Log.e("AutoTradingActivity", "Error updating user balance: $e")
//                        Toast.makeText(this, "Failed to update user balance.", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("AutoTradingActivity", "Error fetching user balance: $e")
//                Toast.makeText(this, "Failed to fetch user balance.", Toast.LENGTH_SHORT).show()
            }
    }
    private fun completePendingRequest(document: DocumentSnapshot) {
        document.reference.update(Constants.KEY_STATUS, "completed")
            .addOnSuccessListener {
                congratulationDialog.ShowDialog("")
//                Toast.makeText(this, "Balance updated and document status updated.", Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "congratulations Balance updated.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("AutoTradingActivity", "Error updating document status: $e")
//                Toast.makeText(this, "Failed to update document status.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun startCountDownTimer(endTimeInMillis: Double, document: DocumentSnapshot, profitAmount: Double) {
        val currentTimeInMillis = System.currentTimeMillis()

        object : CountDownTimer((endTimeInMillis - currentTimeInMillis).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val remainingTimeInSeconds = millisUntilFinished / 1000
                val remainingDays = remainingTimeInSeconds / (24 * 3600)
                val remainingHours = (remainingTimeInSeconds % (24 * 3600)) / 3600
                val remainingMinutes = (remainingTimeInSeconds % 3600) / 60
                val remainingSeconds = remainingTimeInSeconds % 60

                val remainingTime = "Remaining Time: ${remainingDays}d ${remainingHours}h ${remainingMinutes}m ${remainingSeconds}s"
                binding.tvPending.text = remainingTime
            }

            override fun onFinish() {
                binding.tvPending.visibility = View.GONE
                updateBalanceAndCompleteRequest(document, profitAmount)
            }
        }.start()
    }


}