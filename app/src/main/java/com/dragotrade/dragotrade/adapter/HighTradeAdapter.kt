package com.dragotrade.dragotrade.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.model.AutoTradeModel
import com.dragotrade.dragotrade.model.HighTradeModel

class HighTradeAdapter(private val mDataList : ArrayList<HighTradeModel>, val context: Context) : RecyclerView.Adapter<HighTradeAdapter.ViewHolder>(){


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HighTradeAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.trade_history_list_layout,parent,false)

        return ViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: HighTradeAdapter.ViewHolder, position: Int) {
        val trade = mDataList[position]




        if (trade.type == "result") {
            if (trade.result == "win"){
                holder.amount.text = "$" + String.format("%.2f", trade.profitIfWon)
                holder.amount.setTextColor(ContextCompat.getColor(context, R.color.green))
                holder.status.text = "Won"
                holder.status.setBackgroundResource(R.drawable.buy_button_background)

            }else{
                holder.amount.text = "$" + String.format("%.2f", trade.profitIfLost)
                holder.amount.setTextColor(ContextCompat.getColor(context, R.color.red))
                holder.status.text = "Lost"
                holder.status.setBackgroundResource(R.drawable.reject_background)
            }

            holder.date.text = trade.date
            holder.time.text = trade.time

            holder.tvpackage.text = trade.brand

        } else {

            holder.amount.text = "$" + String.format("%.2f", trade.amountSpent)
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.white))

            holder.date.text = trade.date
            holder.time.text = trade.time
            holder.status.text = trade.status
            holder.tvpackage.text = trade.brand

            if (trade.status == "pending"){
                holder.status.setBackgroundResource(R.drawable.pending_background)
            } else if (trade.status == "completed"){
                holder.status.setBackgroundResource(R.drawable.pending_background)
            }
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
        val tvpackage : TextView = itemView.findViewById(R.id.tv_package)
    }
}