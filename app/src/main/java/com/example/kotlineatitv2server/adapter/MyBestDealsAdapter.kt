package com.example.kotlineatitv2server.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlineatitv2server.R
import com.example.kotlineatitv2server.callback.IRecyclerItemClickListener
import com.example.kotlineatitv2server.common.Common
import com.example.kotlineatitv2server.model.BestDealsModel
import com.example.kotlineatitv2server.model.CategoryModel
import com.example.kotlineatitv2server.model.eventbus.CategoryClick
import org.greenrobot.eventbus.EventBus

class MyBestDealsAdapter (internal var context: Context,
                          internal var bestDealsList: List<BestDealsModel>) :
    RecyclerView.Adapter<MyBestDealsAdapter.MyViewHolder>()  {

    override fun onBindViewHolder(holder: MyBestDealsAdapter.MyViewHolder, position: Int) {
        Glide.with(context).load(bestDealsList.get(position).image).into(holder.category_image!!)
        holder.category_name!!.setText(bestDealsList.get(position).name)

        //Event
        holder.setListener(object: IRecyclerItemClickListener {
            override fun onItemClick(view: View, pos: Int) {

            }

        })
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyBestDealsAdapter.MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_category_item,parent,false))

    }

    override fun getItemCount(): Int {
        return bestDealsList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        override fun onClick(view: View?) {
            listener!!.onItemClick(view!!,adapterPosition)
        }

        var category_name: TextView?=null

        var category_image: ImageView?=null

        internal var listener:IRecyclerItemClickListener?=null

        fun setListener(listener: IRecyclerItemClickListener)
        {
            this.listener = listener
        }

        init{
            category_name = itemView.findViewById(R.id.category_name) as TextView
            category_image = itemView.findViewById(R.id.category_image) as ImageView
            itemView.setOnClickListener(this)
        }

    }

}