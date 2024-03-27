package com.dragotrade.dragotrade.screens.transfer

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dragotrade.dragotrade.adapter.TransferAdapter
import com.dragotrade.dragotrade.databinding.ActivityTransferHistoryBinding
import com.dragotrade.dragotrade.model.TransferModel
import com.dragotrade.dragotrade.utils.Constants
import com.dragotrade.dragotrade.utils.LoadingBar
import com.dragotrade.dragotrade.utils.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TransferHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransferHistoryBinding
    lateinit var adapter : TransferAdapter
    private lateinit var mDataList : ArrayList<TransferModel>
    private lateinit var firestore : FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userUID : String
    private lateinit var preferenceManager: PreferenceManager
    private var loadingBar = LoadingBar(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransferHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        userUID = firebaseAuth.currentUser?.uid.toString()
        preferenceManager = PreferenceManager.getInstance(this)

        transferListData()

        binding.backLayout.backImage.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }
        binding.backLayout.backText.text = "Transfer History"

    }
    private fun transferListData() {
        loadingBar.ShowDialog("please wait...")
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)

        mDataList = arrayListOf<TransferModel>()
        adapter = TransferAdapter(mDataList,this , userUID)
        binding.recyclerView.adapter = adapter

        val userQuery = firestore.collection("transfer")
            .whereEqualTo(Constants.KEY_USERUID, userUID)

        val receiverQuery = firestore.collection("transfer")
            .whereEqualTo("receiverID", userUID)

        userQuery.get().addOnSuccessListener { userSnapshot ->
            for (doc in userSnapshot.documents) {
                val transferModel = doc.toObject(TransferModel::class.java)
                if (transferModel != null) {
                    mDataList.add(transferModel)
                }
            }

            receiverQuery.get().addOnSuccessListener { receiverSnapshot ->
                for (doc in receiverSnapshot.documents) {
                    val transferModel = doc.toObject(TransferModel::class.java)
                    if (transferModel != null && !mDataList.contains(transferModel)) {
                        loadingBar.HideDialog()
                        mDataList.add(transferModel)

                    }
                }

                // Sort the list based on timestamp
                mDataList.sortByDescending { it.timestamp }

                adapter.notifyDataSetChanged()
            }.addOnFailureListener { e ->
                loadingBar.HideDialog()
                Log.e("Firestore Error", "Error fetching receiver documents: ${e.message}")
            }
        }.addOnFailureListener { e ->
            loadingBar.HideDialog()
            Log.e("Firestore Error", "Error fetching user documents: ${e.message}")
        }
    }



}