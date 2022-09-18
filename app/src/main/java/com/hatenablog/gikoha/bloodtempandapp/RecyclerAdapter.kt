package com.hatenablog.gikoha.bloodtempandapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

data class BloodTemp (
    val date: String,
    val temp: String,
    val memo: String,
)

class RecyclerAdapter(val list: ArrayList<BloodTemp>) : RecyclerView.Adapter<ViewHolderList>()
{

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): ViewHolderList {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler_list, parent, false)
        return ViewHolderList(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolderList, position: Int) {
        holder.dateField.text = list[position].date
        holder.tempField.text = list[position].temp
        holder.memoField.text = list[position].memo ?: ""
    }

    override fun getItemCount(): Int = list.size
}