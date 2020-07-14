package com.example.kotlineatitv2server.remote

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitFCMClient {
    private var instance: Retrofit?=null

    fun getInstance(): Retrofit {
        if(instance == null)
            instance = Retrofit.Builder().baseUrl("https://fcm.googleapis.com/").addConverterFactory(
                GsonConverterFactory.create()).addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build()
        return instance!!
    }
}