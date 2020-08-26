package com.example.kotlineatitv2server.ui.most_popular

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlineatitv2server.R
import com.example.kotlineatitv2server.adapter.MyBestDealsAdapter
import com.example.kotlineatitv2server.adapter.MyMostPopularAdapter
import com.example.kotlineatitv2server.callback.IMyButtonCallback
import com.example.kotlineatitv2server.common.Common
import com.example.kotlineatitv2server.common.MySwipeHelper
import com.example.kotlineatitv2server.model.BestDealsModel
import com.example.kotlineatitv2server.model.MostPopularModel
import com.example.kotlineatitv2server.model.eventbus.ToastEvent
import com.example.kotlineatitv2server.ui.best_deals.BestDealsViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dmax.dialog.SpotsDialog
import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MostPopularFragment : Fragment() {

    private val PICK_IMAGE_REQUEST: Int = 1234

    private lateinit var viewModel: MostPopularViewModel

    private lateinit var dialog: AlertDialog
    private lateinit var layoutAnimationController: LayoutAnimationController
    private var adapter: MyMostPopularAdapter?=null

    private var recycler_most_popular: RecyclerView?=null

    internal var mostPopularModels:List<MostPopularModel> = ArrayList<MostPopularModel>()
    internal lateinit var storage: FirebaseStorage
    internal lateinit var storageReference: StorageReference
    private var imageUri: Uri?=null
    internal lateinit var img_most_popular: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel =
            ViewModelProviders.of(this).get(MostPopularViewModel::class.java)
        val root = inflater.inflate(R.layout.most_popular_fragment, container, false)

        initView(root)

        viewModel.getMessageError().observe(this, Observer {
            Toast.makeText(context,it, Toast.LENGTH_SHORT).show()
        })
        viewModel.getMostPopulars().observe(this, Observer {
            dialog.dismiss()
            mostPopularModels = it
            adapter = MyMostPopularAdapter(context!!, mostPopularModels)
            recycler_most_popular!!.adapter =  adapter
            recycler_most_popular!!.layoutAnimation = layoutAnimationController
        })
        return root
    }

    private fun initView(root: View?) {
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference


        dialog = SpotsDialog.Builder().setContext(context).setCancelable(false).build()
        dialog.show()
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)

        recycler_most_popular = root!!.findViewById(R.id.recycler_most_popular) as RecyclerView
        recycler_most_popular!!.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)

        recycler_most_popular!!.layoutManager = layoutManager
        recycler_most_popular!!.addItemDecoration(DividerItemDecoration(context,layoutManager.orientation))

        val swipe = object: MySwipeHelper(context!!,recycler_most_popular!!,200)
        {
            override fun instantiateMyButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<MyButton>
            ) {
                buffer.add(MyButton(context!!,
                    "Delete",
                    30,
                    0,
                    Color.parseColor("#333639"),
                    object : IMyButtonCallback {
                        override fun onClick(pos: Int) {
                            Common.mostPopularSelected = mostPopularModels[pos];

                            showDeleteDialog();
                        }

                    }))

                buffer.add(MyButton(context!!,
                    "Update",
                    30,
                    0,
                    Color.parseColor("#560027"),
                    object : IMyButtonCallback {
                        override fun onClick(pos: Int) {
                            Common.mostPopularSelected = mostPopularModels[pos];

                            showUpdateDialog();
                        }

                    }))
            }

        }
    }

    private fun showDeleteDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(context!!)
        builder.setTitle("Delete")
        builder.setMessage("Do you really want to delete this item?")
        builder.setNegativeButton("CANCEL",{dialogInterface, i -> dialogInterface.dismiss()})
        builder.setPositiveButton("DELETE",{dialogInterface, i -> deleteMostPopular()})
        val updateDialog = builder.create()
        updateDialog.show()
    }

    private fun deleteMostPopular() {
        FirebaseDatabase.getInstance()
            .getReference(Common.RESTAURANT_REF)
            .child(Common.currentServerUser!!.restaurant!!)
            .child(Common.MOST_POPULAR)
            .child(Common.mostPopularSelected!!.key!!)
            .removeValue()
            .addOnFailureListener{e-> Toast.makeText(context,""+e.message,Toast.LENGTH_SHORT).show()}
            .addOnCompleteListener{ task ->
                viewModel!!.loadMostPopulars()
                EventBus.getDefault().postSticky(ToastEvent(false,true))
            }
    }

    private fun showUpdateDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(context!!)
        builder.setTitle("Update")
        builder.setMessage("Please fill information")

        val itemView = LayoutInflater.from(context).inflate(R.layout.layout_update_category,null)
        val edt_category_name = itemView.findViewById<View>(R.id.edt_category_name) as EditText
        img_most_popular = itemView.findViewById<View>(R.id.img_category) as ImageView

        //Set data
        edt_category_name.setText(Common.mostPopularSelected!!.name)
        Glide.with(context!!).load(Common.mostPopularSelected!!.image).into(img_most_popular)

        //set event
        img_most_popular.setOnClickListener{ view ->
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST)
        }

        builder.setNegativeButton("CANCEL"){ dialogInterface, _ -> dialogInterface.dismiss() }
        builder.setPositiveButton("UPDATE"){dialogInterface, _ ->
            val updateData = HashMap<String,Any>()
            updateData["name"] = edt_category_name.text.toString()
            if (imageUri != null)
            {
                dialog.setMessage("Uploading....")
                dialog.show()

                val imageName = UUID.randomUUID().toString()
                val imageFolder = storageReference.child("images/$imageName")
                imageFolder.putFile(imageUri!!)
                    .addOnFailureListener{e ->
                        dialog.dismiss()
                        Toast.makeText(context,""+e.message,Toast.LENGTH_SHORT).show()
                    }
                    .addOnProgressListener { taskSnapshot ->
                        val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                        dialog.setMessage("Uploaded $progress")
                    }
                    .addOnSuccessListener { taskSnapshot ->

                        imageFolder.downloadUrl.addOnSuccessListener{uri ->
                            dialogInterface.dismiss()
                            dialog.dismiss()
                            updateData["image"] = uri.toString()
                            updateMostPopular(updateData)
                        }
                    }
            }
            else
            {
                updateMostPopular(updateData)
            }
        }

        builder.setView(itemView)
        val updateDialog = builder.create()
        updateDialog.show()
    }

    private fun updateMostPopular(updateData: java.util.HashMap<String, Any>) {
        FirebaseDatabase.getInstance()
            .getReference(Common.RESTAURANT_REF)
            .child(Common.currentServerUser!!.restaurant!!)
            .child(Common.MOST_POPULAR)
            .child(Common.mostPopularSelected!!.key!!)
            .updateChildren(updateData)
            .addOnFailureListener{e-> Toast.makeText(context,""+e.message,Toast.LENGTH_SHORT).show()}
            .addOnCompleteListener{ task ->
                viewModel!!.loadMostPopulars()
                EventBus.getDefault().postSticky(ToastEvent(true,true))
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK)
        {
            if (data != null && data.data != null)
            {
                imageUri = data.data
                img_most_popular.setImageURI(imageUri)
            }
        }
    }

}