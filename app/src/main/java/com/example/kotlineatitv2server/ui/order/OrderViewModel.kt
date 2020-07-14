package com.example.kotlineatitv2server.ui.order

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlineatitv2server.callback.IOrderCallbackListener
import com.example.kotlineatitv2server.common.Common
import com.example.kotlineatitv2server.model.FoodModel
import com.example.kotlineatitv2server.model.OrderModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList

class OrderViewModel : ViewModel(), IOrderCallbackListener {

private val orderModelList = MutableLiveData<List<OrderModel>>()
    val messageError = MutableLiveData<String>()
    private val orderCallbackListener:IOrderCallbackListener

    init {
        orderCallbackListener=this
    }
    
    fun getOrderModelList():MutableLiveData<List<OrderModel>>{
        loadOrder(0)
        return orderModelList
    }

    fun loadOrder(status: Int) {
        val tempList : MutableList<OrderModel> = ArrayList()
        val orderRef = FirebaseDatabase.getInstance()
            .getReference(Common.ORDER_REF)
            .orderByChild("orderStatus")
            .equalTo(status.toDouble())
        orderRef.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                orderCallbackListener.onOrderLoadFailed(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (itemSnapshot in p0.children)
                {
                    val orderModel = itemSnapshot.getValue(OrderModel::class.java)
                    orderModel!!.key = itemSnapshot.key
                    tempList.add(orderModel)
                }
                orderCallbackListener.onOrderLoadSuccess(tempList)
            }

        })
    }

    override fun onOrderLoadSuccess(orderModel: List<OrderModel>) {
        if (orderModel.size >= 0)
        {
            //Sort Order
            Collections.sort(orderModel){t1,t2->
                if (t1.createDate<t2.createDate) return@sort -1
                if (t1.createDate == t2.createDate) 0 else 1
            }

            orderModelList.value = orderModel
        }
    }

    override fun onOrderLoadFailed(message: String) {
        messageError.value = message
    }

}