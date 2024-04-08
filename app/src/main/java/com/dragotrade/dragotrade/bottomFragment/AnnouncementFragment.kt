package com.dragotrade.dragotrade.bottomFragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.dragotrade.dragotrade.adapter.AnnouncementAdapter
import com.dragotrade.dragotrade.databinding.FragmentAnnouncementBinding
import com.dragotrade.dragotrade.model.AnnouncementModel
import com.dragotrade.dragotrade.utils.Constants
import com.dragotrade.dragotrade.utils.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class AnnouncementFragment : Fragment() {

    private lateinit var binding: FragmentAnnouncementBinding
    lateinit var adapter : AnnouncementAdapter
    private lateinit var mDataList : ArrayList<AnnouncementModel>
    private lateinit var firestore : FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userUID : String
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =  FragmentAnnouncementBinding.inflate(layoutInflater, container, false)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        userUID = firebaseAuth.currentUser?.uid.toString()
        preferenceManager = PreferenceManager.getInstance(requireContext())

        annoucementData()
        return  binding.root
    }


    private fun annoucementData() {
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.setHasFixedSize(true)

        mDataList = arrayListOf<AnnouncementModel>()
        adapter = activity?.let { AnnouncementAdapter(mDataList, it) }!!
        binding.recyclerView.adapter = adapter

        firestore.collection(Constants.COLLECTION_ANNOUNCEMENT)
            .orderBy("timeStamp", Query.Direction.DESCENDING)
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
                            mDataList.add(snapShot.document.toObject(AnnouncementModel::class.java))
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