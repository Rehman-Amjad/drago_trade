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

class DepositAdapter(private val mDataList : ArrayList<DepositModel>, val context: Context) : RecyclerView.Adapter<DepositAdapter.ViewHolder>(){


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DepositAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.deposit_history_list_layout,parent,false)

        return ViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DepositAdapter.ViewHolder, position: Int) {
        val deposit = mDataList[position]

        holder.date.text = deposit.date
        holder.time.text = deposit.time
        holder.status.text = deposit.status
        holder.amount.text = "$" + deposit.amount

        if (deposit.status == "pending"){
            holder.status.setBackgroundResource(R.drawable.pending_background)
        }
        if (deposit.status == "success"){
            holder.status.setBackgroundResource(R.drawable.success_background)
        }
        if (deposit.status == "reject"){
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