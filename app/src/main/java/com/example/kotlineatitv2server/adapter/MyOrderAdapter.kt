package com.example.kotlineatitv2server.adapter

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlineatitv2server.R
import com.example.kotlineatitv2server.callback.IRecyclerItemClickListener
import com.example.kotlineatitv2server.common.Common
import com.example.kotlineatitv2server.model.CartItem
import com.example.kotlineatitv2server.model.OrderModel
import java.text.SimpleDateFormat

class MyOrderAdapter (internal var context: Context,
                      internal var orderList: MutableList<OrderModel>) :
    RecyclerView.Adapter<MyOrderAdapter.MyViewHolder>() {

    lateinit var simpleDateFormat:SimpleDateFormat

    init {
        simpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
    }

    class MyViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var txt_time: TextView?=null
        var txt_order_number: TextView?=null
        var txt_order_status: TextView?=null
        var txt_num_item: TextView?=null
        var txt_name: TextView?=null

        var img_food_image: ImageView?=null

        internal var iRecyclerItemClickListener: IRecyclerItemClickListener?=null

        fun setListener(iRecyclerItemClickListener: IRecyclerItemClickListener)
        {
            this.iRecyclerItemClickListener = iRecyclerItemClickListener
        }

        init {
            img_food_image = itemView.findViewById(R.id.img_food_image) as ImageView

            txt_time = itemView.findViewById(R.id.txt_time) as TextView
            txt_order_number = itemView.findViewById(R.id.txt_order_number) as TextView
            txt_order_status = itemView.findViewById(R.id.txt_order_status) as TextView
            txt_num_item = itemView.findViewById(R.id.txt_num_item) as TextView
            txt_name = itemView.findViewById(R.id.txt_name) as TextView

            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            iRecyclerItemClickListener!!.onItemClick(p0!!,adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_order_item,parent,false))
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(orderList[position].cartItemList!![0].foodImage)
            .into(holder.img_food_image!!)
        holder.txt_order_number!!.setText(orderList[position].key)
        Common.setSpanStringColor("Order date ",simpleDateFormat.format(orderList[position].createDate),
        holder.txt_time,Color.parseColor("#333639"))

        Common.setSpanStringColor("Order status ",Common.convertStatusToString(orderList[position].orderStatus),
            holder.txt_order_status,Color.parseColor("#005758"))

        Common.setSpanStringColor("Num of items ",if (orderList[position].cartItemList == null) "0"
            else orderList[position].cartItemList!!.size.toString(),
            holder.txt_num_item,Color.parseColor("#00574B"))

        Common.setSpanStringColor("Name ",orderList[position].userName,
            holder.txt_name,Color.parseColor("#006061"))

        holder.setListener(object:IRecyclerItemClickListener{
            override fun onItemClick(view: View, pos: Int) {
                showDialog(orderList[pos].cartItemList)
            }
        })
    }

    private fun showDialog(cartItemList: List<CartItem>?) {
        val layout_dialog = LayoutInflater.from(context).inflate(R.layout.layout_dialog_order_detail,null)
        val builder = AlertDialog.Builder(context)
        builder.setView(layout_dialog)
        
        val btn_ok = layout_dialog.findViewById<View>(R.id.btn_ok) as Button
        val recycler_order_detail = layout_dialog.findViewById<View>(R.id.recycler_order_detail) as RecyclerView
        recycler_order_detail.setHasFixedSize(true)
        val layoutManger = LinearLayoutManager(context)
        recycler_order_detail.layoutManager = layoutManger
        recycler_order_detail.addItemDecoration(DividerItemDecoration(context,layoutManger.orientation))
        val adapter = MyOrderDetailAdapter(context,cartItemList!!.toMutableList())
        recycler_order_detail.adapter = adapter

        //Show dialog
        val dialog = builder.create()
        dialog.show()
        //custom dialog
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setGravity(Gravity.CENTER)

        btn_ok.setOnClickListener { dialog.dismiss() }
    }

    fun getItemAtPosition(pos: Int): OrderModel {
        return orderList[pos]
    }

    fun removeItem(pos: Int) {
        orderList.removeAt(pos)
    }


}