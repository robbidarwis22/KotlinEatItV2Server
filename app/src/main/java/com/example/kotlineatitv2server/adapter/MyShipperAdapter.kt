package com.example.kotlineatitv2server.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlineatitv2server.R
import com.example.kotlineatitv2server.callback.IRecyclerItemClickListener
import com.example.kotlineatitv2server.model.CategoryModel
import com.example.kotlineatitv2server.model.ShipperModel
import com.example.kotlineatitv2server.model.eventbus.UpdateActiveEvent
import org.greenrobot.eventbus.EventBus

class MyShipperAdapter (internal var context: Context,
                        internal var shipperList: List<ShipperModel>): RecyclerView.Adapter<MyShipperAdapter.MyViewHolder>() {
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var txt_name: TextView?=null
        var txt_phone: TextView?=null

        var btn_enable: SwitchCompat?=null

        init{
            txt_name = itemView.findViewById(R.id.txt_name) as TextView
            txt_phone = itemView.findViewById(R.id.txt_phone) as TextView
            btn_enable = itemView.findViewById(R.id.btn_enable) as SwitchCompat
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.layout_shipper,parent,false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return shipperList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.txt_name!!.setText(shipperList[position].name)
        holder.txt_phone!!.setText(shipperList[position].phone)
        holder.btn_enable!!.isChecked = shipperList[position].isActive

        //Event
        holder.btn_enable!!.setOnCheckedChangeListener{ compoundButton, b ->
            EventBus.getDefault().postSticky(UpdateActiveEvent(shipperList[position],b))
        }
    }
}