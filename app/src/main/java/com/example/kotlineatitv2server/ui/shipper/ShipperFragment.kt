package com.example.kotlineatitv2server.ui.shipper

import android.app.AlertDialog
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlineatitv2server.R
import com.example.kotlineatitv2server.adapter.MyCategoriesAdapter
import com.example.kotlineatitv2server.adapter.MyShipperAdapter
import com.example.kotlineatitv2server.common.Common
import com.example.kotlineatitv2server.model.ShipperModel
import com.example.kotlineatitv2server.model.eventbus.ChangeMenuClick
import com.example.kotlineatitv2server.model.eventbus.UpdateActiveEvent
import com.google.firebase.database.FirebaseDatabase
import dmax.dialog.SpotsDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ShipperFragment : Fragment() {

    private lateinit var dialog: AlertDialog
    private lateinit var layoutAnimationController: LayoutAnimationController
    private var adapter: MyShipperAdapter?=null
    private var recycler_shipper: RecyclerView?=null

    internal var shipperModels:List<ShipperModel> = ArrayList<ShipperModel>()

    companion object {
        fun newInstance() = ShipperFragment()
    }

    private lateinit var viewModel: ShipperViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val itemView = inflater.inflate(R.layout.fragment_shipper, container, false)
        viewModel = ViewModelProviders.of(this).get(ShipperViewModel::class.java)
        initViews(itemView)

        viewModel.getMessageError().observe(this, Observer {
            Toast.makeText(context,it, Toast.LENGTH_SHORT).show()
        })
        viewModel.getShipperList().observe(this, Observer {
            dialog.dismiss()
            shipperModels = it
            adapter = MyShipperAdapter(context!!, shipperModels)
            recycler_shipper!!.adapter =  adapter
            recycler_shipper!!.layoutAnimation = layoutAnimationController
        })
        return itemView
    }

    private fun initViews(root: View) {
        dialog = SpotsDialog.Builder().setContext(context).setCancelable(false).build()
        dialog.show()
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)

        recycler_shipper = root.findViewById(R.id.recycler_shipper) as RecyclerView
        recycler_shipper!!.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)

        recycler_shipper!!.layoutManager = layoutManager
        recycler_shipper!!.addItemDecoration(DividerItemDecoration(context,layoutManager.orientation))
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onStop() {
        if (EventBus.getDefault().hasSubscriberForEvent(UpdateActiveEvent::class.java))
            EventBus.getDefault().removeStickyEvent(UpdateActiveEvent::class.java)
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
        super.onStop()
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(ChangeMenuClick(true))
        super.onDestroy()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onUpdateActiveEvent(updateActiveEvent: UpdateActiveEvent)
    {
        val updateData = HashMap<String,Any>()
        updateData.put("active",updateActiveEvent.active)
        FirebaseDatabase.getInstance()
            .getReference(Common.SHIPPER_REF)
            .child(updateActiveEvent.shipperModel!!.key!!)
            .updateChildren(updateData)
            .addOnFailureListener{ e -> Toast.makeText(context,""+e.message,Toast.LENGTH_SHORT).show() }
            .addOnSuccessListener { aVoid ->
                Toast.makeText(context,"Update state to "+updateActiveEvent.active,Toast.LENGTH_SHORT).show()
            }
    }
}