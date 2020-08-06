package com.example.kotlineatitv2server.services

import android.content.Intent
import com.example.kotlineatitv2server.MainActivity
import com.example.kotlineatitv2server.common.Common
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class MyFCMServices : FirebaseMessagingService() {
    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Common.updateToken(this,p0,true,false) //because we are in server app so server=true
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val dataRecv = remoteMessage.data
        if(dataRecv != null)
        {
            if (dataRecv[Common.NOTI_TITLE]!!.equals("New Order"))
            {
                //Create intent and call MainActivity
                //Because we need Common.currentUser is assign
                //So we must call MainActivity instead direct HomeActivity
                val intent = Intent(this,MainActivity::class.java)
                intent.putExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER,true) //Pass key
                Common.showNotification(this, Random().nextInt(),
                    dataRecv[Common.NOTI_TITLE],
                    dataRecv[Common.NOTI_CONTENT],
                    intent)
            }
            else
            Common.showNotification(this, Random().nextInt(),
                dataRecv[Common.NOTI_TITLE],
                dataRecv[Common.NOTI_CONTENT],
                null)
        }
    }
}