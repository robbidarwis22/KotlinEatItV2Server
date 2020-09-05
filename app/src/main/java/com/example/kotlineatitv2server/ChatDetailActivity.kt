package com.example.kotlineatitv2server

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.icu.util.Calendar
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlineatitv2server.callback.ILoadTimeFromFirebaseCallback
import com.example.kotlineatitv2server.common.Common
import com.example.kotlineatitv2server.model.ChatInfoModel
import com.example.kotlineatitv2server.model.ChatMessageModel
import com.example.kotlineatitv2server.view_holder.ChatPictureViewHolder
import com.example.kotlineatitv2server.view_holder.ChatTextViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_chat_detail.*
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class ChatDetailActivity : AppCompatActivity(), ILoadTimeFromFirebaseCallback{

    private val MY_CAMERA_REQUEST_CODE = 7171 //any number you want
    private val MY_RESULT_LOAD_IMAGE = 7272 //any number you want

    private var roomId:String?=null
    private var chatSender:String?=null

    var database: FirebaseDatabase?=null
    var chatRef: DatabaseReference?=null
    var offsetRef:DatabaseReference?=null
    var listener: ILoadTimeFromFirebaseCallback?=null

    lateinit var adapter: FirebaseRecyclerAdapter<ChatMessageModel, RecyclerView.ViewHolder>
    lateinit var options: FirebaseRecyclerOptions<ChatMessageModel>

    var fileUri: Uri?= null
    var storageReference: StorageReference?=null
    var layoutManager: LinearLayoutManager?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_detail)

        initViews()
        loadChatContent()
    }

    override fun onStart() {
        super.onStart()
        if (adapter != null) adapter.startListening()
    }

    override fun onResume() {
        super.onResume()
        if (adapter != null) adapter.startListening()
    }

    override fun onStop() {
        if (adapter != null) adapter.stopListening()
        super.onStop()
    }

    private fun loadChatContent() {
        adapter = object :FirebaseRecyclerAdapter<ChatMessageModel,RecyclerView.ViewHolder>(options)
        {
            override fun getItemViewType(position: Int): Int {
                return if (adapter.getItem(position).isPicture) 1 else 0
            }

            override fun onCreateViewHolder(
                viewGroup: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                val view: View
                return if (viewType == 0) {
                    view = LayoutInflater.from(viewGroup.context)
                        .inflate(R.layout.layout_message_text,viewGroup,false)
                    ChatTextViewHolder(view)
                }else{
                    view = LayoutInflater.from(viewGroup.context)
                        .inflate(R.layout.layout_message_picture,viewGroup,false)
                    ChatPictureViewHolder(view)
                }
            }

            override fun onBindViewHolder(
                holder: RecyclerView.ViewHolder,
                position: Int,
                model: ChatMessageModel
            ) {
                if (holder is ChatTextViewHolder)
                {
                    val chatTextViewHolder = holder
                    chatTextViewHolder.txt_email!!.text = model.name
                    chatTextViewHolder.txt_chat_message!!.text = model.content
                    chatTextViewHolder.txt_time!!.text =
                        DateUtils.getRelativeTimeSpanString(model.timeStamp!!,
                            Calendar.getInstance().timeInMillis,0)
                            .toString()
                }
                else
                {
                    val chatPictureViewHolder = holder as ChatPictureViewHolder
                    chatPictureViewHolder.txt_email!!.text = model.name
                    chatPictureViewHolder.txt_chat_message!!.text = model.content
                    chatPictureViewHolder.txt_time!!.text =
                        DateUtils.getRelativeTimeSpanString(model.timeStamp!!,
                            Calendar.getInstance().timeInMillis,0)
                            .toString()
                    Glide.with(this@ChatDetailActivity)
                        .load(model.pictureLink)
                        .into(chatPictureViewHolder.img_preview!!)

                }
            }

        }

        adapter.registerAdapterDataObserver(object :RecyclerView.AdapterDataObserver(){
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                val friendlyMessageCount = adapter.itemCount
                val lastVisiblePosition = layoutManager!!.findLastCompletelyVisibleItemPosition()
                if (lastVisiblePosition == -1 ||
                    positionStart >= friendlyMessageCount -1 &&
                    lastVisiblePosition == positionStart-1)
                {
                    recycler_chat.scrollToPosition(positionStart)
                }
            }
        })

        recycler_chat.adapter = adapter
    }

    private fun initViews() {

        roomId = intent.getStringExtra(Common.KEY_CHAT_ROOM_ID)
        chatSender = intent.getStringExtra(Common.KEY_CHAT_SENDER)

        listener = this
        database = FirebaseDatabase.getInstance()
        chatRef = database!!.getReference(Common.RESTAURANT_REF)
            .child(Common.currentServerUser!!.restaurant!!)
            .child(Common.CHAT_REF)
        offsetRef = database!!.getReference(".info/serverTimeOffset")

        val query = chatRef!!.child(roomId!!).child(Common.CHAT_DETAIL_REF)

        options = FirebaseRecyclerOptions.Builder<ChatMessageModel>()
            .setQuery(query,ChatMessageModel::class.java)
            .build()

        layoutManager = LinearLayoutManager(this)
        recycler_chat.layoutManager = layoutManager

        toolbar.setTitle(chatSender)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        //Event
        img_image.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            startActivityForResult(intent,MY_RESULT_LOAD_IMAGE)
        }
        img_camera.setOnClickListener{
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val builder = StrictMode.VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())

            fileUri = getOutputMediaFileUri() //error
            intent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri)
            startActivityForResult(intent,MY_CAMERA_REQUEST_CODE)
        }
        img_send.setOnClickListener {
            offsetRef!!.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    listener!!.onLoadTimeFailed(p0.message)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    val offset = p0.getValue(Long::class.java)
                    val estimatedServerTimeInMs = System.currentTimeMillis().plus(offset!!)
                    listener!!.onLoadOnlyTimeSuccess(estimatedServerTimeInMs)
                }

            })
        }
    }

    private fun getOutputMediaFileUri(): Uri? {
        return Uri.fromFile(getOutputMediaFile()) //error
    }

    private fun getOutputMediaFile(): File? {
        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "EatItV2")
        if (!mediaStorageDir.exists())
        {
            if (!mediaStorageDir.mkdir()) return null
        }
        val time_stamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return File(StringBuilder(mediaStorageDir.path)
            .append(File.separator)
            .append("IMG_")
            .append(time_stamp)
            .append("_")
            .append(Random().nextInt()).toString())
    }

    private fun submitChatToFirebase(chatMessageModel: ChatMessageModel, isPicture: Boolean,estimateTimeInMs: Long) {
        chatRef!!.child(roomId!!)
            .addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists())
                    {
                        appendChat(chatMessageModel,isPicture,estimateTimeInMs)
                    }
                    else{
                        createChat(chatMessageModel,isPicture,estimateTimeInMs)
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(this@ChatDetailActivity,p0.message,Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun appendChat(
        chatMessageModel: ChatMessageModel,
        picture: Boolean,
        estimateTimeInMs: Long
    ) {
        val update_data = HashMap<String,Any>()
        update_data["lastUpdate"] = estimateTimeInMs
        if (picture)
            update_data["lastMessage"] = "<Image>"
        else
            update_data["lastMessage"] = chatMessageModel.content!!
        chatRef!!.child(roomId!!)
            .updateChildren(update_data)
            .addOnFailureListener { e-> Toast.makeText(this@ChatDetailActivity,e.message,Toast.LENGTH_SHORT).show() }
            .addOnCompleteListener { task2 ->
                if (task2.isSuccessful)
                {
                    chatRef!!.child(roomId!!)
                        .child(Common.CHAT_DETAIL_REF) //add above push()
                        .push()
                        .setValue(chatMessageModel)
                        .addOnFailureListener { e:Exception ->
                            Toast.makeText(this@ChatDetailActivity,e.message,Toast.LENGTH_SHORT).show()
                        }
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful)
                            {
                                edt_chat.setText("")
                                edt_chat.requestFocus()
                                if (adapter != null)
                                {
                                    adapter.notifyDataSetChanged()
                                    if (picture)
                                    {
                                        fileUri = null
                                        img_preview.visibility = View.GONE
                                    }
                                }
                            }
                        }
                }
            }
    }

    private fun createChat(
        chatMessageModel: ChatMessageModel,
        picture: Boolean,
        estimateTimeInMs: Long
    ) {
        val chatInfoModel = ChatInfoModel()
        chatInfoModel.createName = chatMessageModel.name
        if (picture)
            chatInfoModel.lastMessage = "<Image>"
        else
            chatInfoModel.lastMessage = chatMessageModel.content
        chatInfoModel.lastUpdate = estimateTimeInMs
        chatInfoModel.createDate = estimateTimeInMs

        chatRef!!.child(roomId!!)
            .setValue(chatInfoModel)
            .addOnFailureListener { e-> Toast.makeText(this@ChatDetailActivity,e.message,Toast.LENGTH_SHORT).show() }
            .addOnCompleteListener { task2 ->
                if (task2.isSuccessful)
                {
                    chatRef!!.child(roomId!!)
                        .child(Common.CHAT_DETAIL_REF) //add above push()
                        .push()
                        .setValue(chatMessageModel)
                        .addOnFailureListener { e:Exception ->
                            Toast.makeText(this@ChatDetailActivity,e.message,Toast.LENGTH_SHORT).show()
                        }
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful)
                            {
                                edt_chat.setText("")
                                edt_chat.requestFocus()
                                if (adapter != null)
                                {
                                    adapter.notifyDataSetChanged()
                                    if (picture)
                                    {
                                        fileUri = null
                                        img_preview.visibility = View.GONE
                                    }
                                }
                            }
                        }
                }
            }
    }

    private fun uploadPicture(fileUri: Uri, chatMessageModel: ChatMessageModel,estimateTimeInMs:Long) {
        if (fileUri != null)
        {
            val dialog = AlertDialog.Builder(this@ChatDetailActivity)
                .setCancelable(false)
                .setMessage("Please wait...")
                .create()
            dialog.show()
            val fileName = Common.getFileName(contentResolver,fileUri!!)
            val path = StringBuilder(Common.currentServerUser!!.restaurant!!)
                .append("/")
                .append(fileName)
                .toString()
            storageReference = FirebaseStorage.getInstance().getReference(path)
            val uploadTask = storageReference!!.putFile(fileUri)

            uploadTask.continueWithTask { task1 ->
                if (!task1.isSuccessful)
                    Toast.makeText(this@ChatDetailActivity,"Failed to upload",Toast.LENGTH_SHORT).show()
                storageReference!!.downloadUrl
            }.addOnFailureListener { t ->
                dialog.dismiss()
                Toast.makeText(this@ChatDetailActivity,t.message,Toast.LENGTH_SHORT).show()
            }.addOnCompleteListener { task2 ->
                if (task2.isSuccessful)
                {
                    val url = task2.result.toString()
                    dialog.dismiss()
                    chatMessageModel.isPicture = true
                    chatMessageModel.pictureLink = url

                    submitChatToFirebase(chatMessageModel,chatMessageModel.isPicture,estimateTimeInMs)
                }
            }
        }
        else
        {
            Toast.makeText(this@ChatDetailActivity,"Image is empty",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onLoadOnlyTimeSuccess(estimatedTimeMs: Long) {
        val chatMessageModel = ChatMessageModel()
        chatMessageModel.name = Common.currentServerUser!!.name
        chatMessageModel.content = edt_chat.text.toString()
        chatMessageModel.timeStamp = estimatedTimeMs

        if (fileUri == null)
        {
            chatMessageModel.isPicture = false
            submitChatToFirebase(chatMessageModel,chatMessageModel.isPicture,estimatedTimeMs)
        }
        else
        {
            uploadPicture(fileUri!!,chatMessageModel,estimatedTimeMs)
        }
    }

    override fun onLoadTimeFailed(message: String) {
        Toast.makeText(this@ChatDetailActivity,message,Toast.LENGTH_SHORT).show()
    }

    private fun rotateBitmap(bitmap: Bitmap?, i:Int):Bitmap?{
        val matrix = Matrix()
        matrix.postRotate(i.toFloat())
        return Bitmap.createBitmap(bitmap!!,0,0,bitmap.width,
            bitmap.height,matrix,true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_CAMERA_REQUEST_CODE)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                var bitmap: Bitmap?
                var ei: ExifInterface?
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(contentResolver,fileUri)
                    ei = ExifInterface(contentResolver.openInputStream(fileUri!!))
                    val orientation = ei.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED
                    )
                    var rotateBitmap:Bitmap
                    rotateBitmap = when(orientation){
                        ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap,90)!!
                        ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap,180)!!
                        ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap,270)!!
                        else -> bitmap
                    }
                    img_preview.visibility = View.VISIBLE
                    img_preview.setImageBitmap(rotateBitmap)
                }catch (e: FileNotFoundException)
                {
                    e.printStackTrace()
                }catch (e: IOException)
                {
                    e.printStackTrace()
                }
            }
        }
        else if (requestCode == MY_RESULT_LOAD_IMAGE)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                try {
                    val imageUri = data!!.data
                    val inputStream = contentResolver.openInputStream(imageUri!!)
                    val selectedImage = BitmapFactory.decodeStream(inputStream)
                    img_preview.setImageBitmap(selectedImage)
                    img_preview.visibility = View.VISIBLE
                    fileUri = imageUri
                }catch (e:FileNotFoundException)
                {
                    e.printStackTrace()
                }
            }
            else
            {
                Toast.makeText(this@ChatDetailActivity,"You have not select image",Toast.LENGTH_SHORT).show()
            }
        }
    }
}