package com.dragotrade.dragotrade.screens.deposit

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.dragotrade.dragotrade.adapter.DepositAdapter
import com.dragotrade.dragotrade.databinding.ActivityDepositHistoryBinding
import com.dragotrade.dragotrade.model.DepositModel
import com.dragotrade.dragotrade.utils.Constants
import com.dragotrade.dragotrade.utils.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class DepositHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDepositHistoryBinding
    lateinit var adapter : DepositAdapter
    private lateinit var mDataList : ArrayList<DepositModel>
    private lateinit var firestore : FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userUID : String
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDepositHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        userUID = firebaseAuth.currentUser?.uid.toString()
        preferenceManager = PreferenceManager.getInstance(this)

        depositListData()

        binding.backLayout.backImage.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }
        binding.backLayout.backText.text = "Deposit History"
    }

    private fun depositListData() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)

        mDataList = arrayListOf<DepositModel>()
        adapter = DepositAdapter(mDataList,this)
        binding.recyclerView.adapter = adapter
        firestore.collection(Constants.COLLECTION_DEPOSIT)
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
                            mDataList.add(snapShot.document.toObject(DepositModel::class.java))
                        }
                    }
//                    if (mDataList.size <=0)
//                    {
//                        binding.llNoResult.visibility = View.VISIBLE
//                    }else{
//                        binding.llNoResult.visibility = View.GONE
//                    }
                    adapter.notifyDataSetChanged()
                }
            })
    }
}