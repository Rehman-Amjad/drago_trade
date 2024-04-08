package com.dragotrade.dragotrade.screens.trade.highTrade

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.adapter.AutoTradeAdapter
import com.dragotrade.dragotrade.adapter.BrandAdapter
import com.dragotrade.dragotrade.databinding.ActivityHighTradingBinding
import com.dragotrade.dragotrade.model.BrandModel
import com.dragotrade.dragotrade.screens.trade.autoTrade.TradeHistoryActivity
import com.dragotrade.dragotrade.utils.Constants
import com.dragotrade.dragotrade.utils.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class HighTradingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHighTradingBinding

    lateinit var adapter : BrandAdapter
    private lateinit var mDataList : ArrayList<BrandModel>
    private lateinit var firestore : FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userUID : String
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHighTradingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        userUID = firebaseAuth.currentUser?.uid.toString()
        preferenceManager = PreferenceManager.getInstance(this)

        binding.backLayout.backImage.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
            finish()
        }
        binding.backLayout.backText.text = "High Trade"

        binding.tvHistory.setOnClickListener{

            val intent = Intent(this, HighTradeHistoryActivity::class.java)
            startActivity(intent)

        }

        brandList()
        findBalance()
    }
    private fun brandList() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)

        mDataList = arrayListOf<BrandModel>()
        adapter = BrandAdapter(mDataList,this)
        binding.recyclerView.adapter = adapter
        firestore.collection("brands")
            .orderBy(Constants.KEY_TIMESTAMP, Query.Direction.DESCENDING)
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                @SuppressLint("NotifyDataSetChanged")
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?)
                {
                    if (error!=null)
                    {
                        Log.e("Firestore Error", "onEvent: ${error.message.toString()}" )
                        return
                    }
                    for (snapShot : DocumentChange in value?.documentChanges!!)
                    {
                        if (snapShot.type == DocumentChange.Type.ADDED)
                        {
                            mDataList.add(snapShot.document.toObject(BrandModel::class.java))
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            })
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
}