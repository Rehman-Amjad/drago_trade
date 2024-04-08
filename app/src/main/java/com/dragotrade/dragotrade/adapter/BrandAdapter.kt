package com.dragotrade.dragotrade.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.model.AutoTradeModel
import com.dragotrade.dragotrade.model.BrandModel
import com.dragotrade.dragotrade.screens.trade.autoTrade.TradeHistoryActivity
import com.dragotrade.dragotrade.screens.trade.highTrade.ConfirmTradeActivity

class BrandAdapter(private val mDataList : ArrayList<BrandModel>, val context: Context) : RecyclerView.Adapter<BrandAdapter.ViewHolder>(){


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BrandAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.brand_list_layout,parent,false)

        return ViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BrandAdapter.ViewHolder, position: Int) {
        val brand = mDataList[position]

        holder.name.text = brand.brand
        holder.cost.text = brand.rate.toString()

        holder.buy.setOnClickListener{
            val intent = Intent(context, ConfirmTradeActivity::class.java)
            intent.putExtra("name", brand.brand)
            intent.putExtra("rate", brand.rate)
            intent.putExtra("type", "Buy")
            context.startActivity(intent)
            }

        holder.sell.setOnClickListener{
            val intent = Intent(context, ConfirmTradeActivity::class.java)
            intent.putExtra("name", brand.brand)
            intent.putExtra("rate", brand.rate)
            intent.putExtra("type", "Sell")
            context.startActivity(intent)
            }

    }

    override fun getItemCount(): Int {
      return mDataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name : TextView = itemView.findViewById(R.id.tv_name)
        val cost : TextView = itemView.findViewById(R.id.tv_cost)
        val buy : Button = itemView.findViewById(R.id.btn_buy)
        val sell : Button = itemView.findViewById(R.id.btn_sell)

    }
}