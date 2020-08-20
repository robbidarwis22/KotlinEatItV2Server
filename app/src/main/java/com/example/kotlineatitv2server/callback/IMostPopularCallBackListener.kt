package com.example.kotlineatitv2server.callback

import com.example.kotlineatitv2server.model.BestDealsModel
import com.example.kotlineatitv2server.model.MostPopularModel

interface IMostPopularCallBackListener {
    fun onListMostPopularLoadSuccess(mostPopularModels: List<MostPopularModel>)
    fun onListMostPopularLoadFailed(message: String)
}