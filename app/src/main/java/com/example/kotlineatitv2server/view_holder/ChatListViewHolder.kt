package com.example.kotlineatitv2server.view_holder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlineatitv2server.R
import com.example.kotlineatitv2server.callback.IRecyclerItemClickListener
import kotlinx.android.synthetic.main.layout_message_list_item.view.*

class ChatListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var txt_email:TextView
    var txt_chat_message:TextView

    internal var listener:IRecyclerItemClickListener?=null
    fun setListener(listener: IRecyclerItemClickListener)
    {
        this.listener = listener;
    }

    init {
        txt_email = itemView.findViewById(R.id.txt_email) as TextView
        txt_chat_message = itemView.findViewById(R.id.txt_chat_message) as TextView

        itemView.setOnClickListener { view -> listener!!.onItemClick(view,adapterPosition) }
    }
}