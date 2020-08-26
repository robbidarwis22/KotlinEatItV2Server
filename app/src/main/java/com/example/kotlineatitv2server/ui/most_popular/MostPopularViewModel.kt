package com.example.kotlineatitv2server.ui.most_popular

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlineatitv2server.callback.IBestDealCallBackListener
import com.example.kotlineatitv2server.callback.IMostPopularCallBackListener
import com.example.kotlineatitv2server.common.Common
import com.example.kotlineatitv2server.model.BestDealsModel
import com.example.kotlineatitv2server.model.MostPopularModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MostPopularViewModel : ViewModel(), IMostPopularCallBackListener {
    private var mostPopularListMutable : MutableLiveData<List<MostPopularModel>>?=null
    private var messageError: MutableLiveData<String> = MutableLiveData()
    private var mostPopularCallBackListener: IMostPopularCallBackListener

    init {
        mostPopularCallBackListener = this
    }

    fun getMostPopulars() :MutableLiveData<List<MostPopularModel>>{
        if(mostPopularListMutable == null)
        {
            mostPopularListMutable = MutableLiveData()
            loadMostPopulars()
        }
        return mostPopularListMutable!!
    }

    fun loadMostPopulars() {
        val tempList = ArrayList<MostPopularModel>()
        val mostPopularRef = FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_REF)
            .child(Common.currentServerUser!!.restaurant!!)
            .child(Common.MOST_POPULAR)
        mostPopularRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                mostPopularCallBackListener.onListMostPopularLoadFailed((p0.message))
            }
            override fun onDataChange(p0: DataSnapshot) {
                for (itemSnapshot in p0.children)
                {
                    val model = itemSnapshot.getValue<MostPopularModel>(MostPopularModel::class.java)
                    model!!.key = itemSnapshot.key!!
                    tempList.add(model)
                }
                mostPopularCallBackListener.onListMostPopularLoadSuccess(tempList)
            }

        })
    }

    fun getMessageError():MutableLiveData<String>{
        return messageError
    }

    override fun onListMostPopularLoadSuccess(mostPopularModels: List<MostPopularModel>) {
        mostPopularListMutable!!.value = mostPopularModels
    }

    override fun onListMostPopularLoadFailed(message: String) {
        messageError.value = message
    }
}