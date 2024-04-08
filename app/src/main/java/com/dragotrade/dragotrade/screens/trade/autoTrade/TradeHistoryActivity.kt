package com.dragotrade.dragotrade.screens.trade.autoTrade

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dragotrade.dragotrade.adapter.AutoTradeAdapter
import com.dragotrade.dragotrade.databinding.ActivityTradeHistoryBinding
import com.dragotrade.dragotrade.model.AutoTradeModel
import com.dragotrade.dragotrade.utils.Constants
import com.dragotrade.dragotrade.utils.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class TradeHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTradeHistoryBinding

    lateinit var adapter : AutoTradeAdapter
    private lateinit var mDataList : ArrayList<AutoTradeModel>
    private lateinit var firestore : FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userUID : String
    private lateinit var preferenceManager: PreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTradeHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        userUID = firebaseAuth.currentUser?.uid.toString()
        preferenceManager = PreferenceManager.getInstance(this)

        withdrawListData()

        binding.backLayout.backImage.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
            finish()
        }
        binding.backLayout.backText.text = "Trade History"

    }

    private fun withdrawListData() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)

        mDataList = arrayListOf<AutoTradeModel>()
        adapter = AutoTradeAdapter(mDataList,this)
        binding.recyclerView.adapter = adapter
        firestore.collection("autoTrading")
            .whereEqualTo(Constants.KEY_USERUID,userUID)
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
                            mDataList.add(snapShot.document.toObject(AutoTradeModel::class.java))
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            })
    }
}