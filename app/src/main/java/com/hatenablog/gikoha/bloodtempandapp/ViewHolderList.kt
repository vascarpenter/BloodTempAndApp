package com.hatenablog.gikoha.bloodtempandapp

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ViewHolderList(item: View) : RecyclerView.ViewHolder(item) {
    val dateField: TextView = item.findViewById(R.id.recDateField)
    val tempField: TextView = item.findViewById(R.id.recTempField)
    val memoField: TextView = item.findViewById(R.id.recMemoField)
}
