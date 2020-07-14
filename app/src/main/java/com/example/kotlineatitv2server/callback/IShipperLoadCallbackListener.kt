package com.example.kotlineatitv2server.callback

import com.example.kotlineatitv2server.model.ShipperModel

interface IShipperLoadCallbackListener {
    fun onShipperLoadSuccess(shipperList:List<ShipperModel>)
    fun onShipperLoadFailed(message:String)
}