package com.example.kotlineatitv2server.ui.shipper

import android.widget.Button
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlineatitv2server.callback.IShipperLoadCallbackListener
import com.example.kotlineatitv2server.common.Common
import com.example.kotlineatitv2server.model.OrderModel
import com.example.kotlineatitv2server.model.ShipperModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShipperViewModel : ViewModel(), IShipperLoadCallbackListener {

    private var shipperListMutable : MutableLiveData<List<ShipperModel>>?=null
    private var messageError:MutableLiveData<String> = MutableLiveData()
    private var shipperCallbackListener: IShipperLoadCallbackListener

    init {
        shipperCallbackListener = this
    }

    fun getShipperList() :MutableLiveData<List<ShipperModel>>{
        if(shipperListMutable == null)
        {
            shipperListMutable = MutableLiveData()
            loadShipper()
        }
        return shipperListMutable!!
    }

    fun loadShipper() {
        val tempList = ArrayList<ShipperModel>()
        val shipperRef = FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_REF)
            .child(Common.currentServerUser!!.restaurant!!)
            .child(Common.SHIPPER_REF)
        shipperRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                shipperCallbackListener.onShipperLoadFailed((p0.message))
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (itemSnapshot in p0.children)
                {
                    val model = itemSnapshot.getValue<ShipperModel>(ShipperModel::class.java)
                    model!!.key = itemSnapshot.key
                    tempList.add(model)
                }
                shipperCallbackListener.onShipperLoadSuccess(tempList)
            }


        })
    }

    fun getMessageError():MutableLiveData<String>{
        return messageError
    }

    override fun onShipperLoadSuccess(shipperList: List<ShipperModel>) {
        shipperListMutable!!.value = shipperList
    }

    override fun onShipperLoadSuccess(
        pos: Int,
        orderModel: OrderModel?,
        shipperList: List<ShipperModel>?,
        dialog: AlertDialog?,
        ok: Button?,
        cancel: Button?,
        rdi_shipping: RadioButton?,
        rdi_shipped: RadioButton?,
        rdi_cancelled: RadioButton?,
        rdi_delete: RadioButton?,
        rdi_restore_placed: RadioButton?
    ) {
        //Do nothing
    }

    override fun onShipperLoadFailed(message: String) {
        messageError.value = message
    }

}