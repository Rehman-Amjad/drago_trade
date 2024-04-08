package com.dragotrade.dragotrade.screens.trade.highTrade

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.ActivityConfirmTradeBinding
import com.dragotrade.dragotrade.screens.trade.autoTrade.AutoSlipActivity
import com.dragotrade.dragotrade.utils.CongratulationDialog
import com.dragotrade.dragotrade.utils.Constants
import com.dragotrade.dragotrade.utils.CurrentDateTime
import com.dragotrade.dragotrade.utils.LoadingBar
import com.dragotrade.dragotrade.utils.PreferenceManager
import com.dragotrade.dragotrade.utils.TimeFrame
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class ConfirmTradeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfirmTradeBinding
    private lateinit var name : String
    private lateinit var rate : String
    private lateinit var type : String
    private var profit : Double = 0.0
    private var loss : Double = 0.0

    private lateinit var userUID : String
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    private var loadingBar  = LoadingBar(this)
    private var congratulationDialog = CongratulationDialog(this)
    private var currentDateTime  = CurrentDateTime(this)
    private lateinit var preferenceManager: PreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmTradeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        userUID = firebaseAuth.currentUser?.uid.toString()
        preferenceManager = PreferenceManager.getInstance(this)

        getIntentData()
        findBalance()
        //check the clost time to text trade
        updateClosestBetTime(currentDateTime)

        binding.backLayout.backImage.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
            finish()
        }
        binding.backLayout.backText.text = "Confirm Trade"

        binding.confirmButton.setOnClickListener{
            loadingBar.ShowDialog("please wait...")
            if(isValid()){
                getTimeFramesFromFirestore(currentDateTime)
            }

        }

        binding.edAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed, but we have to implement it
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed, but we have to implement it
            }

            override fun afterTextChanged(s: Editable?) {
                // Calculate profit based on the entered amount and selected days
                val enteredAmount = s.toString().toDoubleOrNull() ?: 0.0

                profit = enteredAmount * 10 / 100
                loss = enteredAmount * 15 / 100

                binding.tvPercentageProfit.setTextColor(ContextCompat.getColor(this@ConfirmTradeActivity, R.color.green))
                binding.tvPercentageLoss.setTextColor(ContextCompat.getColor(this@ConfirmTradeActivity, R.color.red))

                binding.tvPercentageProfit.text = "$ " + String.format("%.2f", profit)
                binding.tvPercentageLoss.text = "-$ " + String.format("%.2f", loss)
                binding.tvAmount.text = "$ " + String.format("%.2f", enteredAmount)
            }
        })
    }

    private fun isValid(): Boolean {
        val enteredAmount = binding.edAmount.text.toString().toDoubleOrNull() ?: 0.0
        val userBalance = binding.tvBalance.text.toString().toDoubleOrNull() ?: 0.0

        if(binding.edAmount.text.isEmpty()){
            loadingBar.HideDialog()
            Toast.makeText(this,"Amount is require        d.",Toast.LENGTH_SHORT).show()
            return false
        }

        if ((loss + enteredAmount) > userBalance) {
            loadingBar.HideDialog()
            Toast.makeText(this, "Insufficient balance.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true

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

    private fun getTimeFramesFromFirestore(currentDateTime: CurrentDateTime) {
        firestore.collection("TimeFrames")
            .get()
            .addOnSuccessListener { documents ->
                val timeFrames = mutableListOf<TimeFrame>()
                for (document in documents) {
                    val startHour = document.getLong("startHour") ?: 0
                    val startMinute = document.getLong("startMinute") ?: 0
                    val endHour = document.getLong("endHour") ?: 0
                    val endMinute = document.getLong("endMinute") ?: 0
                    timeFrames.add(TimeFrame(startHour.toInt(), startMinute.toInt(), endHour.toInt(), endMinute.toInt()))
                }

                // Check if current time falls within any of the retrieved time frames
                if (isTimeInAnyFrame(currentDateTime, timeFrames)) {
                    // Action allowed
                    checkPendingRequests()
                } else {
                    // Action not allowed
                    loadingBar.HideDialog()
                    Toast.makeText(this, "Button not available at the moment", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                loadingBar.HideDialog()
                println("Error getting time frames: $e")
                // Handle failure
            }
    }

    private fun isTimeInAnyFrame(currentDateTime: CurrentDateTime, timeFrames: List<TimeFrame>): Boolean {
        val currentTime = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
        }

        for (frame in timeFrames) {
            if (currentDateTime.isTimeInFrame(
                    frame.startHour,
                    frame.startMinute,
                    frame.endHour,
                    frame.endMinute
                )
            ) {
                return true
            }
        }
        return false
    }

    private fun checkPendingRequests() {
        firestore.collection("highTrading")
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

        // Calculate daily profit
        val profitIfWon = (profit + enteredAmount)
        val profitIfLost = (loss + enteredAmount)

        val id = firestore.collection("highTrading").document().id
        // Trade request details
        val tradeRequest = hashMapOf(
            Constants.KEY_USERUID to preferenceManager.getString(Constants.KEY_USERUID),
            Constants.KEY_USERNAME to preferenceManager.getString(Constants.KEY_USERNAME),
            Constants.KEY_FULLNAME to preferenceManager.getString(Constants.KEY_FULLNAME),
            Constants.KEY_ID to id,
            Constants.KEY_STATUS to "pending",
            "type" to "bought",
            "result" to "empty",
            "brand" to name,
            "userChoice" to type,
            Constants.KEY_DATE to currentDateTime.getCurrentDate(),
            Constants.KEY_TIME to currentDateTime.getTimeWithAmPm(),
            Constants.KEY_TIMESTAMP to currentDateTime.getTimeMiles(),
            "profitIfWon" to profitIfWon,
            "profitIfLost" to profitIfLost,
            "amountSpent" to enteredAmount
        )

        // Submit main trade request to Firestore
        firestore.collection("highTrading").document(id)
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

                        val intent = Intent(this, HighSlipActivity::class.java)
                        startActivity(intent)
                        finish()

                        loadingBar.HideDialog()
                    }
                    .addOnFailureListener { e ->
                        // Failed to update balance
                        Log.e("HighTradingActivity", "Error updating balance: $e")
                        Toast.makeText(this, "Failed to update balance", Toast.LENGTH_SHORT).show()
                        loadingBar.HideDialog()
                    }
            }
            .addOnFailureListener { e ->
                // Failed to submit trade request
                Log.e("HighTradingActivity", "Error submitting trade request: $e")
                Toast.makeText(this, "Failed to submit trade request", Toast.LENGTH_SHORT).show()
                loadingBar.HideDialog()
            }
    }


    //Save the time frame
    private fun saveTimeFrames() {
        val timeFrames = listOf(
            hashMapOf(
                "startHour" to 12,
                "startMinute" to 0,
                "endHour" to 12,
                "endMinute" to 30
            ),
            hashMapOf(
                "startHour" to 15,
                "startMinute" to 0,
                "endHour" to 15,
                "endMinute" to 30
            ),
            hashMapOf(
                "startHour" to 18,
                "startMinute" to 0,
                "endHour" to 18,
                "endMinute" to 30
            )
        )

        for ((index, timeFrame) in timeFrames.withIndex()) {
            firestore.collection("TimeFrames")
                .document("TimeFrame$index")
                .set(timeFrame)
                .addOnSuccessListener {
                    println("Time frame $index saved successfully")
                    Toast.makeText(this,"Time frame saved.",Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    println("Error saving time frame $index: $e")
                }
        }
    }
private fun updateClosestBetTime(currentDateTime: CurrentDateTime) {
    val currentTime = Calendar.getInstance()

    firestore.collection("TimeFrames")
        .get()
        .addOnSuccessListener { documents ->
            val timeFrames = mutableListOf<TimeFrame>()
            for (document in documents) {
                val startHour = document.getLong("startHour") ?: 0
                val startMinute = document.getLong("startMinute") ?: 0
                val endHour = document.getLong("endHour") ?: 0
                val endMinute = document.getLong("endMinute") ?: 0
                timeFrames.add(TimeFrame(startHour.toInt(), startMinute.toInt(), endHour.toInt(), endMinute.toInt()))
            }

            // Find the closest time frame to the current time
            var closestFrame: TimeFrame? = null
            var closestDifference = Long.MAX_VALUE
            for (frame in timeFrames) {
                val frameTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, frame.startHour)
                    set(Calendar.MINUTE, frame.startMinute)
                    set(Calendar.SECOND, 0)
                }
                val difference = Math.abs(currentTime.timeInMillis - frameTime.timeInMillis)
                if (difference < closestDifference) {
                    closestDifference = difference
                    closestFrame = frame
                }
            }

            closestFrame?.let { frame ->
                val startTime = formatTime(frame.startHour, frame.startMinute)
                val endTime = formatTime(frame.endHour, frame.endMinute)
                val timeRange = "$startTime - $endTime"
                binding.tvTime.text = timeRange
            }
        }
        .addOnFailureListener { e ->
            println("Error getting time frames: $e")
            // Handle failure
        }
}

    private fun formatTime(hour: Int, minute: Int): String {
        val hourOfDay = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
        val amPm = if (hour < 12) "AM" else "PM"
        return String.format("%02d:%02d %s", hourOfDay, minute, amPm)
    }


    private fun getIntentData() {
        name = intent.getStringExtra("name").toString()
        rate = intent.getStringExtra("rate").toString()
        type = intent.getStringExtra("type").toString()

        if (type == "Buy") {
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.green))

        } else {
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.red))
        }

        binding.tvStatus.text = type
        binding.tvBrand.text = name
        binding.tvTitle.text = "You have selected "+ name

    }
}