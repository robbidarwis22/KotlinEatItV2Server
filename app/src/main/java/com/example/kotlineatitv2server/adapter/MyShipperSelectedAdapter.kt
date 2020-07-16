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
import com.example.kotlineatitv2server.model.ShipperModel
import com.example.kotlineatitv2server.model.eventbus.UpdateActiveEvent
import org.greenrobot.eventbus.EventBus

class MyShipperSelectedAdapter (internal var context: Context,
                        internal var shipperList: List<ShipperModel>): RecyclerView.Adapter<MyShipperSelectedAdapter.MyViewHolder>() {

    var lastCheckedImageView:ImageView?= null
    var selectedShipper:ShipperModel?=null
    private set

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        var txt_name: TextView?=null
        var txt_phone: TextView?=null

        var img_checked: ImageView?=null

        var iRecyclerItemClickListener:IRecyclerItemClickListener?=null

        fun setClick(iRecyclerItemClickListener: IRecyclerItemClickListener)
        {
            this.iRecyclerItemClickListener = iRecyclerItemClickListener
        }

        init{
            txt_name = itemView.findViewById(R.id.txt_name) as TextView
            txt_phone = itemView.findViewById(R.id.txt_phone) as TextView
            img_checked = itemView.findViewById(R.id.img_checked) as ImageView

            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            iRecyclerItemClickListener!!.onItemClick(p0!!,adapterPosition)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.layout_shipper_selected,parent,false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return shipperList.size
    }



    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.txt_name!!.setText(shipperList[position].name)
        holder.txt_phone!!.setText(shipperList[position].phone)
        holder.setClick(object:IRecyclerItemClickListener{
            override fun onItemClick(view: View, pos: Int) {
                if (lastCheckedImageView != null)
                    lastCheckedImageView!!.setImageResource(0)
                holder.img_checked!!.setImageResource(R.drawable.ic_baseline_check_24)
                lastCheckedImageView = holder.img_checked
                selectedShipper = shipperList[pos]
            }

        })
    }
}