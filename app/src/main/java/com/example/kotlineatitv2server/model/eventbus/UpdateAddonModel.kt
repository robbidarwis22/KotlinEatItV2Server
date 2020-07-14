package com.example.kotlineatitv2server.model.eventbus

import com.example.kotlineatitv2server.model.AddonModel
import com.example.kotlineatitv2server.model.SizeModel

class UpdateAddonModel {
    var addonModelList: List<AddonModel>? =null
    constructor(){}
    constructor(addonModelList: List<AddonModel>?){
        this.addonModelList = addonModelList
    }


}