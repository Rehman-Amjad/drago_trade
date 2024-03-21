package com.dragotrade.dragotrade.utils


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.TextView
import com.dragotrade.dragotrade.R

// custom class for progress loading bar
class LoadingBar constructor(context: Context?) {

    var context: Context? = context
    var dialog: Dialog? = null

    // this function to show loading bar
    fun ShowDialog(title: String?) {
        dialog = Dialog(context!!)
        dialog!!.setContentView(R.layout.loading_dialog_layout)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val dialog_title_text = dialog!!.findViewById<TextView>(R.id.dialog_title_text)
        dialog_title_text.text = title
        dialog!!.create()
        dialog!!.show()
    }
    // this function to dismiss the loading bar
    fun HideDialog() {
        dialog!!.dismiss()
    }


}