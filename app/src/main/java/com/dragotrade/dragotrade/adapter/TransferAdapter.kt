package com.dragotrade.dragotrade.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.model.TransferModel

class TransferAdapter(private val mDataList: ArrayList<TransferModel>, val context: Context, val yourUserUID: String) : RecyclerView.Adapter<TransferAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransferAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.transfer_history_list_layout, parent, false)
        return ViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TransferAdapter.ViewHolder, position: Int) {
        val transfer = mDataList[position]

//        holder.date.text = transfer.date
//        holder.time.text = transfer.time
//        holder.amount.text = "$" + transfer.amount
//
////        // Check if the user's userUID is equal to the receiverID
////        val isUserReceiver = transfer.receiverID == yourUserUID
////
////        // Set the status text based on whether the user is the sender or receiver
////        holder.status.text = if (isUserReceiver) "Received" else "Sent"

        holder.date.text = transfer.date
        holder.time.text = transfer.time

        val isUserReceiver = transfer.receiverID == yourUserUID

        // Prepend the amount with "+" for received transfers and "-" for sent transfers
        val amountPrefix = if (isUserReceiver) "+" else "-"
        holder.amount.text = "$amountPrefix${"$" + transfer.amount}"

        // Set the color of the status text based on whether it's a sent or received transfer
        val colorRes = if (isUserReceiver) R.color.white else R.color.app_color
        holder.amount.setTextColor(ContextCompat.getColor(context, colorRes))

        holder.status.text = if (isUserReceiver) "Received" else "Sent"

    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.date)
        val time: TextView = itemView.findViewById(R.id.time)
        val amount: TextView = itemView.findViewById(R.id.amount)
        val status: TextView = itemView.findViewById(R.id.status)
        val list_item: LinearLayout = itemView.findViewById(R.id.list_item)
    }
}
