package com.example.kotlineatitv2server.remote

import com.example.kotlineatitv2server.model.FCMResponse
import com.example.kotlineatitv2server.model.FCMSendData
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFCMService {
    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAAUWXd2iE:APA91bEzlP4JAydPtd9VSCwtmA7k3FjuShxO9e9jMoEcdwxQuidH24vZu7Xct0vT42qmKe4xly88VDe1QYwnQcWjE3XI2QtfsaId2dOoFA_psDhpvV0xIMqovx3-Ytof2nEzO0V9e-7X"
    )
    @POST("fcm/send")
    fun sendNotification(@Body body: FCMSendData): Observable<FCMResponse>
}