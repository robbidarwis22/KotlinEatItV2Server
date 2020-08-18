package com.example.kotlineatitv2server.ui.best_deals

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
import com.example.kotlineatitv2server.adapter.MyBestDealsAdapter
import com.example.kotlineatitv2server.adapter.MyCategoriesAdapter
import com.example.kotlineatitv2server.model.BestDealsModel
import com.example.kotlineatitv2server.model.CategoryModel
import com.example.kotlineatitv2server.ui.category.CategoryViewModel
import dmax.dialog.SpotsDialog

class BestDealsFragment : Fragment() {

    private lateinit var viewModel: BestDealsViewModel

    private lateinit var dialog: AlertDialog
    private lateinit var layoutAnimationController: LayoutAnimationController
    private var adapter: MyBestDealsAdapter?=null

    private var recycler_best_deals: RecyclerView?=null

    internal var bestDealsModels:List<BestDealsModel> = ArrayList<BestDealsModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel =
            ViewModelProviders.of(this).get(BestDealsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_best_deals, container, false)

        initView(root)

        viewModel.getMessageError().observe(this, Observer {
            Toast.makeText(context,it, Toast.LENGTH_SHORT).show()
        })
        viewModel.getBestDealsList().observe(this, Observer {
            dialog.dismiss()
            bestDealsModels = it
            adapter = MyBestDealsAdapter(context!!, bestDealsModels)
            recycler_best_deals!!.adapter =  adapter
            recycler_best_deals!!.layoutAnimation = layoutAnimationController
        })
        return root
    }

    private fun initView(root: View?) {
        dialog = SpotsDialog.Builder().setContext(context).setCancelable(false).build()
        dialog.show()
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)

        recycler_best_deals = root!!.findViewById(R.id.recycler_best_deal) as RecyclerView
        recycler_best_deals!!.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)

        recycler_best_deals!!.layoutManager = layoutManager
        recycler_best_deals!!.addItemDecoration(DividerItemDecoration(context,layoutManager.orientation))
    }


}