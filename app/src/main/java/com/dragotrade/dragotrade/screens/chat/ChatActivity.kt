package com.dragotrade.dragotrade.screens.chat

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.adapter.ChatAdapter
import com.dragotrade.dragotrade.databinding.ActivityChatBinding
import com.dragotrade.dragotrade.model.ChatModel
import com.dragotrade.dragotrade.utils.Constants
import com.dragotrade.dragotrade.utils.CurrentDateTime
import com.dragotrade.dragotrade.utils.LoadingBar
import com.dragotrade.dragotrade.utils.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

class ChatActivity : AppCompatActivity(),View.OnClickListener {

    private lateinit var binding: ActivityChatBinding
    private lateinit var preferenceManager: PreferenceManager

    lateinit var adapter : ChatAdapter
    private lateinit var mDataList : ArrayList<ChatModel>

    private lateinit var firestore : FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userUID : String
    private var loadingBar = LoadingBar(this)
    private var currentDateTime = CurrentDateTime(this)
    var uri: Uri? = null
    var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        userUID = firebaseAuth.currentUser?.uid.toString()
        preferenceManager = PreferenceManager.getInstance(this)

        setListener()
        getChatList()


    }

    private fun setListener() {
        binding.progressBarWaiting.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        binding.btnSend.setOnClickListener(this)
    }

    private fun getChatList() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)

        mDataList = arrayListOf<ChatModel>()
        adapter = ChatAdapter(mDataList,this)
        binding.recyclerView.adapter = adapter
        firestore.collection(Constants.COLLECTION_CHATS)
            .document(preferenceManager.getString(Constants.KEY_USERUID))
            .collection(Constants.COLLECTION_CHATS)
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
                            mDataList.add(snapShot.document.toObject(ChatModel::class.java))
                        }
                    }
                    if (mDataList.size <=0)
                    {
                        binding.llNoChat.visibility = View.VISIBLE
                        binding.progressBarWaiting.visibility = View.GONE
                    }else{
                        binding.llNoChat.visibility = View.GONE
                        binding.progressBarWaiting.visibility = View.GONE
                    }
                    adapter.notifyDataSetChanged()
                }
            })
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.btn_send -> validates()
        }
    }

    private fun validates() {
        binding.apply {
            if (edMessage.text.isNullOrEmpty()){
                showMessage("Please enter message")
            } else{
                binding.progressBar.visibility = View.VISIBLE
                binding.btnSend.visibility = View.GONE
               // loadingBar.ShowDialog("please wait")
               sendMessages(binding.edMessage.text.toString())
            }
        }
    }

    private fun sendMessages(message: String) {
        val map = hashMapOf(
            Constants.KEY_EMAIL to preferenceManager.getString(Constants.KEY_EMAIL),
            Constants.KEY_USERNAME to preferenceManager.getString(Constants.KEY_USERNAME),
            Constants.KEY_USERUID to preferenceManager.getString(Constants.KEY_USERUID),
            Constants.KEY_FULLNAME to preferenceManager.getString(Constants.KEY_FULLNAME),
        )
        firestore.collection(Constants.COLLECTION_CHATS)
            .document(userUID)
            .set(map).addOnCompleteListener{ task->
                if (task.isSuccessful){
                    saveMessageData(message)
                }
            }
    }

    private fun saveMessageData(message: String){
        val timeStamp = currentDateTime.getTimeMiles().toString();
        val map = hashMapOf(
            Constants.KEY_TIMESTAMP to timeStamp,
            Constants.KEY_DATE to currentDateTime.getCurrentDate().toString(),
            Constants.KEY_TIME to currentDateTime.getTimeWithAmPm().toString(),
            Constants.KEY_NOTIFICATION_MESSAGE to message,
            "type" to "user",
        )
        firestore.collection(Constants.COLLECTION_CHATS)
            .document(userUID)
            .collection(Constants.COLLECTION_CHATS)
            .document(timeStamp)
            .set(map).addOnCompleteListener{ task->
                if (task.isSuccessful){
                    binding.edMessage.setText("")
                }
            }.addOnCompleteListener {
                if (it.isSuccessful){
                    binding.btnSend.setVisibility(View.VISIBLE)
                    binding.image.setVisibility(View.GONE)
                    binding.progressBar.visibility = View.GONE
                }
            }
    }

    private fun showMessage(message: String) {

        Toast.makeText(this@ChatActivity, message, Toast.LENGTH_SHORT).show()
    }
}