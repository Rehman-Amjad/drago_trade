package com.dragotrade.dragotrade.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.model.ChatModel
import com.dragotrade.dragotrade.utils.DateTimeUtils

class ChatAdapter(private val mDataList : ArrayList<ChatModel>, val context: Context) : RecyclerView.Adapter<ChatAdapter.ViewHolder>(){


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.chat_list_layout,parent,false)

        return ViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ChatAdapter.ViewHolder, position: Int) {
        val chat = mDataList[position]
        holder.ll_other.visibility = View.GONE
        holder.ll_my.visibility = View.GONE

        if (chat.type.equals("admin")) {
//            if (model.getImageUrl().equals("")) {
//                holder.my_image.setVisibility(View.GONE)
//            } else {
//                holder.my_image.setVisibility(View.VISIBLE)
//                Glide.with(context).load(model.getImageUrl()).into(holder.my_image)
//            }
            holder.ll_other.visibility = View.VISIBLE
            holder.tv_other.text = chat.message
            holder.tv_other_time.text = chat.timestamp?.toLong()
                ?.let { DateTimeUtils().getRelativeTimeWithDetails(it) }
            holder.tv_other_time.text = ""
        }

        if (chat.type.equals("user")) {
//            if (model.getImageUrl().equals("")) {
//                holder.my_image.setVisibility(View.GONE)
//            } else {
//                holder.my_image.setVisibility(View.VISIBLE)
//                Glide.with(context).load(model.getImageUrl()).into(holder.my_image)
//            }
            holder.ll_my.visibility = View.VISIBLE
            holder.tv_my.text = chat.message
            holder.tv_my_time.text = chat.timestamp?.toLong()
                ?.let { DateTimeUtils().getRelativeTimeWithDetails(it) }
            holder.tv_my_date.text = "Deliver"
        }

    }

    override fun getItemCount(): Int {
      return mDataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_other : TextView = itemView.findViewById(R.id.tv_other)
        val tv_other_time : TextView = itemView.findViewById(R.id.tv_other_time)
        val tv_other_date : TextView = itemView.findViewById(R.id.tv_other_date)
        val tv_my : TextView = itemView.findViewById(R.id.tv_my)
        val tv_my_time : TextView = itemView.findViewById(R.id.tv_my_time)
        val tv_my_date : TextView = itemView.findViewById(R.id.tv_my_date)
        val ll_other : LinearLayout = itemView.findViewById(R.id.ll_other)
        val ll_my : LinearLayout = itemView.findViewById(R.id.ll_my)
    }
}