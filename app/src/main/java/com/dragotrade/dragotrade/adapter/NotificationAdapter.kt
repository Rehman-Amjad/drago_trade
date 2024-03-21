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
import com.dragotrade.dragotrade.model.NotificationModel

class NotificationAdapter(private val mDataList : ArrayList<NotificationModel>, val context: Context) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>(){


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificationAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.notification_layout,parent,false)

        return ViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: NotificationAdapter.ViewHolder, position: Int) {
        val notification = mDataList[position]

        holder.title.text = notification.title
        holder.message.text = notification.message
        holder.dateTime.text = "${notification.date} | ${notification.time}"
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