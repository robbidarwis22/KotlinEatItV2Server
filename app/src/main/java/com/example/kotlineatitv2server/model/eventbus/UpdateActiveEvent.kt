package com.example.kotlineatitv2server.model.eventbus

import com.example.kotlineatitv2server.model.ShipperModel

class UpdateActiveEvent(var shipperModel: ShipperModel,var active:Boolean) {
}