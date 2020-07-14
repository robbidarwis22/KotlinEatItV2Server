package com.example.kotlineatitv2server.callback

import com.example.kotlineatitv2server.model.CategoryModel

interface ICategoryCallBackListener {
    fun onCategoryLoadSuccess(categoriesList:List<CategoryModel>)
    fun onCategoryLoadFailed(message:String)
}