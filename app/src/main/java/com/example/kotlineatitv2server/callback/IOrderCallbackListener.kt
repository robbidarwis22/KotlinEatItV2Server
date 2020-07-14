package com.example.kotlineatitv2server.callback

import com.example.kotlineatitv2server.model.OrderModel


interface IOrderCallbackListener {
    fun onOrderLoadSuccess(orderModel :List<OrderModel>)
    fun onOrderLoadFailed(message:String)
}