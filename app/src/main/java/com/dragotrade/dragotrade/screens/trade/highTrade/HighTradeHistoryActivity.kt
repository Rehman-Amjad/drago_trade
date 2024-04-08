package com.dragotrade.dragotrade.screens.trade.highTrade

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dragotrade.dragotrade.adapter.HighTradeAdapter
import com.dragotrade.dragotrade.databinding.ActivityHighTradeHistoryBinding
import com.dragotrade.dragotrade.model.HighTradeModel
import com.dragotrade.dragotrade.utils.Constants
import com.dragotrade.dragotrade.utils.LoadingBar
import com.dragotrade.dragotrade.utils.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class HighTradeHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHighTradeHistoryBinding

    lateinit var adapter1 : HighTradeAdapter
    private lateinit var mDataList : ArrayList<HighTradeModel>
    private lateinit var firestore : FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userUID : String
    private lateinit var preferenceManager: PreferenceManager
    private var loadingBar = LoadingBar(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHighTradeHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        userUID = firebaseAuth.currentUser?.uid.toString()
        preferenceManager = PreferenceManager.getInstance(this)

        HighListData()

        binding.backLayout.backImage.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
            finish()
        }
        binding.backLayout.backText.text = "Trade History"
    }

    private fun HighListData() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)

        mDataList = arrayListOf<HighTradeModel>()
        adapter1 = HighTradeAdapter(mDataList,this)
        binding.recyclerView.adapter = adapter1

        firestore.collection("highTrading")
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
                            mDataList.add(snapShot.document.toObject(HighTradeModel::class.java))
                        }
                    }
                    adapter1.notifyDataSetChanged()
                }
            })
    }
}
