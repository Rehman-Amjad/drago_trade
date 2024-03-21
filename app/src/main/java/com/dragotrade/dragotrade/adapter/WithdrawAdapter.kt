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
import com.dragotrade.dragotrade.model.DepositModel
import com.dragotrade.dragotrade.model.NotificationModel
import com.dragotrade.dragotrade.model.WithdrawModel

class WithdrawAdapter(private val mDataList : ArrayList<WithdrawModel>, val context: Context) : RecyclerView.Adapter<WithdrawAdapter.ViewHolder>(){


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WithdrawAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.deposit_history_list_layout,parent,false)

        return ViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: WithdrawAdapter.ViewHolder, position: Int) {
        val withdraw = mDataList[position]

        holder.date.text = withdraw.date
        holder.time.text = withdraw.time
        holder.status.text = withdraw.status
        holder.amount.text = "$" + withdraw.amount

        if (withdraw.status == "pending"){
            holder.status.setBackgroundResource(R.drawable.pending_background)
        }
        if (withdraw.status == "success"){
            holder.status.setBackgroundResource(R.drawable.success_background)
        }
        if (withdraw.status == "reject"){
            holder.status.setBackgroundResource(R.drawable.reject_background)
        }
    }

    override fun getItemCount(): Int {
      return mDataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date : TextView = itemView.findViewById(R.id.date)
        val time : TextView = itemView.findViewById(R.id.time)
        val status : TextView = itemView.findViewById(R.id.status)
        val amount : TextView = itemView.findViewById(R.id.amount)
    }
}