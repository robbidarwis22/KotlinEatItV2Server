package com.example.kotlineatitv2server.callback

interface ILoadTimeFromFirebaseCallback {
    fun onLoadOnlyTimeSuccess(estimatedTimeMs:Long)
    fun onLoadTimeFailed(message:String)
}