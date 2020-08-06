package com.example.kotlineatitv2server.callback

import com.example.kotlineatitv2server.model.ShippingOrderModel

interface ISingleShippingOrderCallbackListener {
    fun onSingleShippingOrderSuccess(shippingOrderModel: ShippingOrderModel)
}