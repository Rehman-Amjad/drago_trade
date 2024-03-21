package com.dragotrade.dragotrade.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.model.AnnouncementModel
import com.dragotrade.dragotrade.model.NotificationModel

class AnnouncementAdapter(private val mDataList : ArrayList<AnnouncementModel>, val context: Context) : RecyclerView.Adapter<AnnouncementAdapter.ViewHolder>(){


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AnnouncementAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.annoucement_list,parent,false)

        return ViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: AnnouncementAdapter.ViewHolder, position: Int) {
        val model = mDataList[position]

        holder.title.text = model.title
        holder.message.text = model.desc
//        holder.dateTime.text = "${notification.date} | ${notification.time}"
    }

    override fun getItemCount(): Int {
      return mDataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title : TextView = itemView.findViewById(R.id.title)
        val message : TextView = itemView.findViewById(R.id.message)
        val dateTime : TextView = itemView.findViewById(R.id.dateTime)
    }
}