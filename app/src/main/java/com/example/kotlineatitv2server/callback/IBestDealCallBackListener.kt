package com.example.kotlineatitv2server.callback

import com.example.kotlineatitv2server.model.BestDealsModel

interface IBestDealCallBackListener {
    fun onListBestDealsLoadSuccess(bestDealsModels: List<BestDealsModel>)
    fun onListBestDealsLoadFailed(message: String)
}