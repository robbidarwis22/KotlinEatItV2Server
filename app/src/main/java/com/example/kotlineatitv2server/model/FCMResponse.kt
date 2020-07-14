package com.example.kotlineatitv2server.model

class FCMResponse {
    var multicast_id:Long?=0
    var success:Int=0
    var failure:Int=0
    var canonical_ids:Int=0
    var results:List<FCMResult>?=null
    var message_id:Long=0
}