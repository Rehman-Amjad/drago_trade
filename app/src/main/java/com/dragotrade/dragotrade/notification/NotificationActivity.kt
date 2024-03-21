package com.dragotrade.dragotrade.notification

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.adapter.NotificationAdapter
import com.dragotrade.dragotrade.databinding.ActivityNotificationBinding
import com.dragotrade.dragotrade.model.NotificationModel
import com.dragotrade.dragotrade.utils.Constants
import com.dragotrade.dragotrade.utils.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding
    lateinit var adapter : NotificationAdapter
    private lateinit var mDataList : ArrayList<NotificationModel>
    private lateinit var firestore : FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userUID : String
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        userUID = firebaseAuth.currentUser?.uid.toString()
        preferenceManager = PreferenceManager.getInstance(this)

        notificationListData()

        binding.backLayout.backImage.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }
        binding.backLayout.backText.text = "Notifications"


    }

    private fun notificationListData() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)

        mDataList = arrayListOf<NotificationModel>()
        adapter = NotificationAdapter(mDataList,this)
        binding.recyclerView.adapter = adapter
        firestore.collection(Constants.COLLECTION_USER)
            .document(userUID)
            .collection("notification")
//            .orderBy(Constants.KEY_TIMESTAMP,Query.Direction.ASCENDING)
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
                            mDataList.add(snapShot.document.toObject(NotificationModel::class.java))
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