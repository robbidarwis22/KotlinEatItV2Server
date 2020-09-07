package com.example.kotlineatitv2server.model.eventbus

import com.example.kotlineatitv2server.model.OrderModel

class PrintOrderEvent(var path:String,var orderModel:OrderModel)