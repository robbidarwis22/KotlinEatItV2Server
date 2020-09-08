package com.example.kotlineatitv2server.model.eventbus

import com.example.kotlineatitv2server.common.Common

class ToastEvent(var action: Common.ACTION, var isBackFromFoodList:Boolean)