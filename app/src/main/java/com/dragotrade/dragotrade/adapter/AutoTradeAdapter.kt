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

class AutoTradeAdapter(private val mDataList : ArrayList<AutoTradeModel>, val context: Context) : RecyclerView.Adapter<AutoTradeAdapter.ViewHolder>(){


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AutoTradeAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.trade_history_list_layout,parent,false)

        return ViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: AutoTradeAdapter.ViewHolder, position: Int) {
        val trade = mDataList[position]

        if (trade.type == "bought") {
            holder.amount.text = "$" + String.format("%.2f", trade.amountSpent)
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.red))

            holder.date.text = trade.date
            holder.time.text = trade.time
            holder.status.text = "Bought"
            holder.tvpackage.visibility = View.GONE
            holder.status.setBackgroundResource(R.drawable.reject_background)

        } else {

            holder.amount.text = "$" + String.format("%.2f", trade.profit)
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.white))

            holder.date.text = trade.date
            holder.time.text = trade.time
            holder.status.text = trade.status
            holder.tvpackage.text = trade.days?.toInt().toString() + " Day(s)"

            if (trade.status == "pending"){
                holder.status.setBackgroundResource(R.drawable.pending_background)
            } else if (trade.status == "completed"){
                holder.status.setBackgroundResource(R.drawable.success_background)
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