package com.dragotrade.dragotrade

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dragotrade.dragotrade.databinding.ActivityMainBinding
import androidx.viewpager2.widget.ViewPager2
import com.dragotrade.dragotrade.start.LoginActivity
import com.dragotrade.dragotrade.utils.CongratulationDialog
import com.dragotrade.dragotrade.utils.Constants
import com.dragotrade.dragotrade.utils.CurrentDateTime
import com.dragotrade.dragotrade.utils.LoadingBar
import com.dragotrade.dragotrade.utils.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import nl.joery.animatedbottombar.AnimatedBottomBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity(), LogoutListener {


    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ViewPagerAdapter

    private lateinit var userUID : String
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var preferenceManager: PreferenceManager
    private var currentDateTime  = CurrentDateTime(this)
    private var congratulationDialog = CongratulationDialog(this)
    private var loadingBar  = LoadingBar(this)

    private var backPressTime: Long = 0
    private val BACK_PRESS_DELAY = 2000

    private var profitIfLost : Double = 0.0
    private var profitIfWon : Double = 0.0
    private lateinit var result : String
    private lateinit var brand : String

    override fun onBackPressed() {
        if (backPressTime + BACK_PRESS_DELAY > System.currentTimeMillis()) {
            super.onBackPressed()
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }
        backPressTime = System.currentTimeMillis()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        userUID = firebaseAuth.currentUser?.uid.toString()
        preferenceManager = PreferenceManager.getInstance(this)

        adapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        binding.fragmentContainer.adapter = adapter
        binding.fragmentContainer.offscreenPageLimit = 1

        // Set the default fragment to "HomeFragment"
        binding.fragmentContainer.setCurrentItem(0, false)
        binding.fragmentContainer.isUserInputEnabled = false

        loadingBar.ShowDialog("please Wait")
        getRequestDate()
        getHighTradeDate()

//        binding.bottomBar.onTabSelected = {
//            Log.d("bottom_bar", "Selected tab: " + it.title)
//        }
//        binding.bottomBar.onTabReselected = {
//            Log.d("bottom_bar", "Reselected tab: " + it.title)
//        }


        binding.bottomBar.setOnTabSelectListener(object : AnimatedBottomBar.OnTabSelectListener {
            override fun onTabSelected(
                lastIndex: Int,
                lastTab: AnimatedBottomBar.Tab?,
                newIndex: Int,
                newTab: AnimatedBottomBar.Tab
            ) {
                binding.fragmentContainer.setCurrentItem(newIndex, false)
                Log.d("bottom_bar", "Selected index: $newIndex, title: ${newTab.title}")
            }

            // An optional method that will be fired whenever an already selected tab has been selected again.
            override fun onTabReselected(index: Int, tab: AnimatedBottomBar.Tab) {
                Log.d("bottom_bar", "Reselected index: $index, title: ${tab.title}")
            }
        })

        binding.fragmentContainer.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    4 -> {
                        // logout functions
//                        onLogout()
                    }
                }

            }
        })

    }

    private fun getHighTradeDate() {
        firestore.collection("highTrading")
            .whereEqualTo(Constants.KEY_USERUID, userUID)
            .whereEqualTo(Constants.KEY_STATUS, "pending")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    profitIfLost = document.getDouble("profitIfLost")!!
                    profitIfWon = document.getDouble("profitIfWon")!!
                    result = document.getString("result").toString()
                    brand = document.getString("brand").toString()

                    // Check if result is "win"
                    if (result == "win") {
                        if (profitIfWon != null) {
                            updateBalance(document, profitIfWon, true)
                        }
                    } else if (result == "lose") { // Check if result is "lose"
                        if (profitIfLost != null) {
                            updateBalance(document, profitIfLost, false)
                        }
                    } else {
                        loadingBar.HideDialog()
                    }
                }
            }
            .addOnFailureListener { e ->
                // Handle failure
                loadingBar.HideDialog()
                Log.e("AutoTradingActivity", "Error checking pending request date: $e")
//                Toast.makeText(this, "Failed to check pending request date.", Toast.LENGTH_SHORT).show()
            }
    }
    private fun updateBalance(document: DocumentSnapshot, profitAmount: Double, isWin: Boolean) {
        // Update user balance in the wallet
        firestore.collection(Constants.COLLECTION_WALLET).document(userUID)
            .get()
            .addOnSuccessListener { walletDoc ->
                val currentBalance = walletDoc.getDouble(Constants.KEY_BALANCE) ?: 0.0
                val newBalance = if (isWin) currentBalance + profitAmount else currentBalance - profitAmount

                // Update balance in the wallet
                firestore.collection(Constants.COLLECTION_WALLET).document(userUID)
                    .update(Constants.KEY_BALANCE, newBalance)
                    .addOnSuccessListener {
                        // Update the document status to "completed"
                        updatePendingRequest(document)
                    }
                    .addOnFailureListener { e ->
                        loadingBar.HideDialog()
                        Log.e("AutoTradingActivity", "Error updating user balance: $e")
                        // Handle failure to update balance
                    }
            }
            .addOnFailureListener { e ->
                loadingBar.HideDialog()
                Log.e("AutoTradingActivity", "Error fetching user balance: $e")
                // Handle failure to fetch balance
            }
    }


    override fun onLogout() {
//        preferenceManager.putBoolean(Constants.USER_STATUS, false)
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

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
                        val daysDifference = currentDateTime.getDaysDifference(currentDate, tradeDate)

                        if (daysDifference != null) {
                            if (daysDifference >= daysFromDocument) {
                                updateBalanceAndCompleteRequest(document, profitAmount)
                            } else {
                                loadingBar.HideDialog()
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                // Handle failure
                loadingBar.HideDialog()
                Log.e("AutoTradingActivity", "Error checking pending request date: $e")
//                Toast.makeText(this, "Failed to check pending request date.", Toast.LENGTH_SHORT).show()
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
                        loadingBar.HideDialog()
                        Log.e("AutoTradingActivity", "Error updating user balance: $e")
//                        Toast.makeText(this, "Failed to update user balance.", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                loadingBar.HideDialog()
                Log.e("AutoTradingActivity", "Error fetching user balance: $e")
//                Toast.makeText(this, "Failed to fetch user balance.", Toast.LENGTH_SHORT).show()
            }
    }
    private fun completePendingRequest(document: DocumentSnapshot) {
        document.reference.update(Constants.KEY_STATUS, "completed")
            .addOnSuccessListener {
                congratulationDialog.ShowDialog("")
                loadingBar.HideDialog()
                Toast.makeText(this, "congratulations Balance updated.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                loadingBar.HideDialog()
                Log.e("AutoTradingActivity", "Error updating document status: $e")
            }
    }

    private fun updatePendingRequest(document: DocumentSnapshot) {
        document.reference.update(Constants.KEY_STATUS, "completed")
            .addOnSuccessListener {
                congratulationDialog.ShowDialog("")

                Toast.makeText(this, "Congratulations! Balance updated.", Toast.LENGTH_SHORT).show()

                // Create a new document in TradeResult collection
                val tradeResultData = hashMapOf(
                    Constants.KEY_USERUID to preferenceManager.getString(Constants.KEY_USERUID),
                    Constants.KEY_USERNAME to preferenceManager.getString(Constants.KEY_USERNAME),
                    Constants.KEY_FULLNAME to preferenceManager.getString(Constants.KEY_FULLNAME),
                    "result" to result,
                    "brand" to brand,
                    "type" to "result",
                    "profitIfWon" to profitIfWon,
                    "profitIfLost" to profitIfLost,
                    Constants.KEY_DATE to currentDateTime.getCurrentDate(),
                    Constants.KEY_TIME to currentDateTime.getTimeWithAmPm(),
                    Constants.KEY_TIMESTAMP to currentDateTime.getTimeMiles(),
                )

                firestore.collection("highTrading")
                    .add(tradeResultData)
                    .addOnSuccessListener { documentReference ->
                        loadingBar.HideDialog()
                        Log.d("AutoTradingActivity", "TradeResult document created with ID: ${documentReference.id}")
                    }
                    .addOnFailureListener { e ->
                        loadingBar.HideDialog()
                        Log.e("AutoTradingActivity", "Error creating TradeResult document: $e")
                    }
            }
            .addOnFailureListener { e ->
                loadingBar.HideDialog()
                Log.e("AutoTradingActivity", "Error updating document status: $e")
            }
    }

}