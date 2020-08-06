package com.example.kotlineatitv2server

import android.animation.ValueAnimator
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.kotlineatitv2server.callback.IShipperLoadCallbackListener
import com.example.kotlineatitv2server.callback.ISingleShippingOrderCallbackListener
import com.example.kotlineatitv2server.common.Common
import com.example.kotlineatitv2server.model.ShippingOrderModel
import com.example.kotlineatitv2server.remote.IGoogleAPI
import com.example.kotlineatitv2server.remote.RetrofitGoogleAPIClient

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject

class TrackingOrderActivity : AppCompatActivity(), OnMapReadyCallback,
    ISingleShippingOrderCallbackListener, ValueEventListener {

    private var isInit: Boolean=false
    private lateinit var shippingRef: DatabaseReference
    private var currentShippingOrder: ShippingOrderModel?=null
    private lateinit var mMap: GoogleMap
    private var iSingleShippingOrderCallbackListener:ISingleShippingOrderCallbackListener?=null

    //Routes
    private var shipperMarker: Marker?=null
    private var polylineOptions:PolylineOptions?=null
    private var blackPolylineOptions:PolylineOptions?=null
    private var blackPolyline:Polyline?=null
    private var grayPolyline:Polyline?=null
    private var redPolyline:Polyline?=null
    private var polylineList:List<LatLng> = ArrayList()

    private lateinit var iGoogleAPI: IGoogleAPI
    private val compositeDisposable = CompositeDisposable()

    //MoveMarker
    private var handler: Handler?=null
    private var index=0
    private var next:Int=0
    private var v=0f
    private var lat = 0.0
    private var lng = 0.0
    private var startPosition=LatLng(0.0,0.0)
    private var endPosition=LatLng(0.0,0.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking_order)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        initViews()
    }

    private fun initViews() {
        iSingleShippingOrderCallbackListener = this
        iGoogleAPI = RetrofitGoogleAPIClient.instance!!.create(IGoogleAPI::class.java)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap!!.uiSettings.isZoomControlsEnabled = true
        try {
            val success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.uber_light_with_label))
            if (!success)
                Log.d("Darwis","Failed to load map style")
        }catch (ex: Resources.NotFoundException)
        {
            Log.d("Darwis","Not found json string for map style")
        }

        checkOrderFromFirebase()
    }

    private fun checkOrderFromFirebase() {
        FirebaseDatabase.getInstance()
            .getReference(Common.SHIPPING_ORDER_REF)
            .child(Common.currentOrderSelected!!.orderNumber!!)
            .addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(this@TrackingOrderActivity,p0.message,Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists())
                    {
                        val shippingOrderModel = p0.getValue(ShippingOrderModel::class.java)
                        shippingOrderModel!!.key = p0.key

                        iSingleShippingOrderCallbackListener!!.onSingleShippingOrderSuccess(shippingOrderModel)
                    }
                    else
                    {
                        Toast.makeText(this@TrackingOrderActivity,"Order not found",Toast.LENGTH_SHORT).show()
                    }
                }

            })
    }

    override fun onSingleShippingOrderSuccess(shippingOrderModel: ShippingOrderModel) {
        currentShippingOrder = shippingOrderModel
        subscribeShipperMove(currentShippingOrder!!)

        val locationOrder = LatLng(
            shippingOrderModel.orderModel!!.lat,
            shippingOrderModel.orderModel!!.lng)
        val locationShipper = LatLng(
            shippingOrderModel.currentLat,
            shippingOrderModel.currentLng)

        //Addbox
        mMap.addMarker(MarkerOptions()
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.box))
            .title(shippingOrderModel!!.orderModel!!.userName)
            .snippet(shippingOrderModel!!.orderModel!!.shippingAddress)
            .position(locationOrder))

        //add shipper
        if (shipperMarker == null)
        {
            val height = 80
            val width = 80
            val bitmapDrawable = ContextCompat.getDrawable(this@TrackingOrderActivity,R.drawable.shippernew)
                    as BitmapDrawable
            val resized = Bitmap.createScaledBitmap(bitmapDrawable.bitmap,width,height,false)

            shipperMarker =  mMap.addMarker(MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resized))
                .title(shippingOrderModel!!.shipperName)
                .snippet(shippingOrderModel!!.shipperPhone)
                .position(locationShipper))

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationShipper,18.0f))
        }
        else
        {
            shipperMarker!!.position = locationShipper
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationShipper,18.0f))
        }

        //Draw route
        val to = StringBuilder().append(shippingOrderModel!!.orderModel!!.lat)
            .append(",")
            .append(shippingOrderModel!!.orderModel!!.lng)
            .toString()

        val from = StringBuilder().append(shippingOrderModel!!.currentLat)
            .append(",")
            .append(shippingOrderModel!!.currentLng)
            .toString()

        compositeDisposable.add(iGoogleAPI!!.getDirections("driving","less_driving",
            from,to,
            getString(R.string.google_maps_key))!!
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ s->
                try {
                    val jsonObject = JSONObject(s)
                    val jsonArray = jsonObject.getJSONArray("routes")
                    for (i in 0 until jsonArray.length())
                    {
                        val route = jsonArray.getJSONObject(i)
                        val poly = route.getJSONObject("overview_polyline")
                        val polyline = poly.getString("points")
                        polylineList = Common.decodePoly(polyline)
                    }

                    polylineOptions = PolylineOptions()
                    polylineOptions!!.color(Color.RED)
                    polylineOptions!!.width(12.0f)
                    polylineOptions!!.startCap(SquareCap())
                    polylineOptions!!.endCap(SquareCap())
                    polylineOptions!!.jointType(JointType.ROUND)
                    polylineOptions!!.addAll(polylineList)
                    redPolyline = mMap.addPolyline(polylineOptions)



                }catch (e:Exception)
                {
                    Log.d("DEBUG",e.message)
                }

            },{throwable ->
                Toast.makeText(this@TrackingOrderActivity,""+throwable.message,Toast.LENGTH_SHORT).show()
            }))
    }

    private fun subscribeShipperMove(currentShippingOrder: ShippingOrderModel) {
        shippingRef = FirebaseDatabase.getInstance()
            .getReference(Common.SHIPPING_ORDER_REF)
            .child(currentShippingOrder!!.key!!)
        shippingRef.addValueEventListener(this)

    }

    override fun onCancelled(p0: DatabaseError) {
        Toast.makeText(this@TrackingOrderActivity,p0.message,Toast.LENGTH_SHORT).show()
    }

    override fun onDataChange(p0: DataSnapshot) {
        if (p0.exists())
        {
            //save old position
            val from:String = java.lang.StringBuilder()
                .append(currentShippingOrder!!.currentLat)
                .append(",")
                .append(currentShippingOrder!!.currentLng)
                .toString()
            //Update position
            currentShippingOrder = p0.getValue(ShippingOrderModel::class.java)
            //save new position
            val to:String = java.lang.StringBuilder()
                .append(currentShippingOrder!!.currentLat)
                .append(",")
                .append(currentShippingOrder!!.currentLng)
                .toString()

            if (isInit) moveMakerAnimation(shipperMarker,from,to) else isInit=true
        }
    }

    private fun moveMakerAnimation(shipperMarker: Marker?, from: String, to: String) {
        compositeDisposable.add(iGoogleAPI!!.getDirections("driving",
            "less_driving",
            from,
            to,
            getString(R.string.google_maps_key))!!
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ s->
                Log.d("DEBUG",s)
                try {
                    val jsonObject = JSONObject(s)
                    val jsonArray = jsonObject.getJSONArray("routes")
                    for (i in 0 until jsonArray.length())
                    {
                        val route = jsonArray.getJSONObject(i)
                        val poly = route.getJSONObject("overview_polyline")
                        val polyline = poly.getString("points")
                        polylineList = Common.decodePoly(polyline)
                    }

                    polylineOptions = PolylineOptions()
                    polylineOptions!!.color(Color.GRAY)
                    polylineOptions!!.width(5.0f)
                    polylineOptions!!.startCap(SquareCap())
                    polylineOptions!!.endCap(SquareCap())
                    polylineOptions!!.jointType(JointType.ROUND)
                    polylineOptions!!.addAll(polylineList)
                    grayPolyline = mMap.addPolyline(polylineOptions)

                    blackPolylineOptions = PolylineOptions()
                    blackPolylineOptions!!.color(Color.BLACK)
                    blackPolylineOptions!!.width(5.0f)
                    blackPolylineOptions!!.startCap(SquareCap())
                    blackPolylineOptions!!.endCap(SquareCap())
                    blackPolylineOptions!!.jointType(JointType.ROUND)
                    blackPolylineOptions!!.addAll(polylineList)
                    blackPolyline = mMap.addPolyline(blackPolylineOptions)

                    //Animator
                    val polylineAnimator = ValueAnimator.ofInt(0,100)
                    polylineAnimator.setDuration(2000)
                    polylineAnimator.setInterpolator(LinearInterpolator())
                    polylineAnimator.addUpdateListener { valueAnimator ->
                        val points = grayPolyline!!.points
                        val percentValue = Integer.parseInt(valueAnimator.animatedValue.toString())
                        val size = points.size
                        val newPoints = (size*(percentValue / 100.0f)).toInt()
                        val p = points.subList(0,newPoints)
                        blackPolyline!!.points = p

                    }
                    polylineAnimator.start()

                    //Car moving
                    index = -1
                    next = 1
                    val r = object: Runnable {
                        override fun run() {
                            if (index < polylineList.size - 1)
                            {
                                index++
                                next = index + 1
                                startPosition = polylineList[index]
                                endPosition = polylineList[next]
                            }

                            val valueAnimator = ValueAnimator.ofInt(0,1)
                            valueAnimator.setDuration(1500)
                            valueAnimator.setInterpolator(LinearInterpolator())
                            valueAnimator.addUpdateListener { valueAnimator ->
                                v = valueAnimator.animatedFraction
                                lat = v * endPosition!!.latitude + (1-v) * startPosition!!.latitude
                                lng = v * endPosition!!.longitude + (1-v) * startPosition!!.longitude

                                val newPos = LatLng(lat,lng)
                                shipperMarker!!.position = newPos
                                shipperMarker!!.setAnchor(0.5f,0.5f)
                                shipperMarker!!.rotation = Common.getBearing(startPosition!!,newPos)

                                mMap.moveCamera(CameraUpdateFactory.newLatLng(shipperMarker.position)) //Fixed

                            }

                            valueAnimator.start()
                            if (index < polylineList.size - 2)
                                handler!!.postDelayed(this,1500)
                        }

                    }

                    handler = Handler()
                    handler!!.postDelayed(r,1500)



                }catch (e:Exception)
                {
                    Log.d("DEBUG",e.message)
                }

            },{throwable ->
                Toast.makeText(this@TrackingOrderActivity,""+throwable.message,Toast.LENGTH_SHORT).show()
            }))
    }

    override fun onDestroy() {
        shippingRef.removeEventListener(this)
        isInit=false
        super.onDestroy()
    }
}