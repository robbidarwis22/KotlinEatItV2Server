package com.example.kotlineatitv2server.model.eventbus

import com.example.kotlineatitv2server.model.SizeModel

class UpdateSizeModel {
    var sizeModelList: List<SizeModel>? =null
    constructor(){}
    constructor(sizeMdelList: List<SizeModel>?){
        this.sizeModelList = sizeModelList
    }


}