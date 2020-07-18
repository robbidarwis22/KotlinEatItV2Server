package com.example.kotlineatitv2server.model

class ShippingOrderModel {
    var shipperPhone:String?=null
    var shipperName:String?=null
    var currentLat= 0.0
    var currentLng= 0.0
    var orderModel:OrderModel?=null
    var isStartTrip=false
}