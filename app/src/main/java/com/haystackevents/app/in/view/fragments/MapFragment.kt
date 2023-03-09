package com.haystackevents.app.`in`.view.fragments

import android.Manifest
import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.RotateAnimation
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity.RESULT_ERROR
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.databinding.FragmentMapSearchBinding
import com.haystackevents.app.`in`.manager.SessionManager
import com.haystackevents.app.`in`.network.repository.Repository
import com.haystackevents.app.`in`.network.response.near_events.NearEvents
import com.haystackevents.app.`in`.network.response.near_events.NearEventsData
import com.haystackevents.app.`in`.network.response.post_data.GetNearEvents
import com.haystackevents.app.`in`.network.response.search_events.SearchByEvent
import com.haystackevents.app.`in`.utils.AppConstants.ARG_OBJECTS
import com.haystackevents.app.`in`.utils.AppConstants.ARG_SERIALIZABLE
import com.haystackevents.app.`in`.utils.AppConstants.PERMISSION_REQ_LOCATION
import com.haystackevents.app.`in`.utils.AppConstants.USER_LATITUDE
import com.haystackevents.app.`in`.utils.AppConstants.USER_LONGITUDE
import com.haystackevents.app.`in`.utils.Extensions.getCurrentDate
import com.haystackevents.app.`in`.utils.Extensions.getCurrentTime
import com.haystackevents.app.`in`.utils.Extensions.hideKeyboard
import com.haystackevents.app.`in`.utils.Extensions.showAlertDialog
import com.haystackevents.app.`in`.utils.FragmentCallback
import com.haystackevents.app.`in`.utils.ProgressCaller
import com.haystackevents.app.`in`.view.activity.MainMenuActivity
import com.haystackevents.app.`in`.view.adapters.NearEventsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*


@Suppress("DEPRECATION")
class MapFragment: Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    LocationListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraIdleListener,
    GoogleMap.OnCameraMoveStartedListener {

    private lateinit var supportMapFragment: SupportMapFragment
    private var lastLocation: Location? = null
    private lateinit var nearEventsAdapter: NearEventsAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var binding: FragmentMapSearchBinding? = null
    private lateinit var mMap: GoogleMap
    private lateinit var geocoder: Geocoder
    private var currentmarker: Marker? = null
    private var fields: List<Place.Field>? = null
    private var nearEventsList = arrayListOf<NearEventsData>()

    private var searchEvent: SearchByEvent? = null
    private var nationWide: String? = "0"
    private var distanceInMile: String? = "0"
    private lateinit var nearEvent: GetNearEvents
    private var listLatLng = arrayListOf<MapMarkers>()
    private var lastClickTime: Long = 0
    private var currentLatLng: LatLng? = null

    private var country: String? = null
    private var state: String? = null
    private var zip: String? = null
    private var city: String? = null
    private var latitude: String? = null
    private var longitude: String? = null
    private var isNearEventsCalled:Boolean = false


    data class MapMarkers(
        val latlng: LatLng,
        val event: String)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapSearchBinding.inflate(layoutInflater)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initiateView()

        clickListeners()

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun clickListeners() {
        binding?.linearLayout?.layoutTransition?.enableTransitionType(LayoutTransition.CHANGING) /**Layout transition enable here*/
        binding?.sliderButton?.layoutTransition?.enableTransitionType(LayoutTransition.CHANGING) /**Layout transition enable here*/

        binding?.toolbarSearch?.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding?.bottomSheetLayout?.btnContinue?.setOnClickListener {
            searchEvent?.nationWide = nationWide!!
            searchEvent?.searchType = "automatically"
            if (country != null)searchEvent?.country = country!!
            if (state != null)searchEvent?.state = state!!
            if (zip != null)searchEvent!!.zipcode = zip!!
            if (city != null)searchEvent?.city = city!!
            if (latitude != null)searchEvent?.latitude = latitude!!
            if (longitude != null)searchEvent?.longitude = longitude!!
            searchEvent?.distanceMile = binding?.bottomSheetLayout?.mapRadius?.text.toString().trim()

            val bundle = bundleOf(ARG_SERIALIZABLE to searchEvent)
            findNavController().navigate(R.id.action_searchFragment_to_dateRangeFragment, bundle)
        }

        binding?.bottomSheetLayout?.btnManualSearch?.setOnClickListener {
            searchEvent?.nationWide = nationWide!!
            searchEvent?.searchType = "manual"
            searchEvent?.distanceMile = distanceInMile
            val bundle = bundleOf(ARG_SERIALIZABLE to searchEvent)
            findNavController().navigate(R.id.action_eventsMapFragment_to_manualSearchMapScreen, bundle)
        }

        binding?.addressSearchView?.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action){
                MotionEvent.ACTION_UP -> {
                    if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                        return@setOnTouchListener false
                    }
                    lastClickTime = SystemClock.elapsedRealtime()
                    val intent = Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, fields!!
                    ).build(requireContext())
                    resultLauncher.launch(intent)
                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }
        }

        binding?.bottomSheetLayout?.checkNationWide?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                nationWide = "1"
                distanceInMile = "0"
                binding?.bottomSheetLayout?.mapRadius?.setText("0")
                binding?.bottomSheetLayout?.layoutMapRadius?.visibility = GONE
            }
            else {
                binding?.bottomSheetLayout?.layoutMapRadius?.visibility = VISIBLE
                nationWide = "0"
                distanceInMile = binding?.bottomSheetLayout?.mapRadius?.text.toString().trim()
            }
            val latLng = if (latitude != null && longitude != null) LatLng(latitude?.toDouble()!!,longitude?.toDouble()!!)
            else LatLng(lastLocation?.latitude!!, lastLocation?.longitude!!)
            if (!isNearEventsCalled) nearestEvents(latLng)
//            if (lastLocation != null) {
//                lastLocation?.latitude?.let { latitude ->
//                    lastLocation?.longitude?.let { longitude ->
//                    LatLng(latitude, longitude)
//                } }?.let { nearestEvents(it) }
//            }
        }

        binding?.bottomSheetLayout?.mapRadius?.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_DONE){
                if (!TextUtils.isEmpty(distanceInMile)) {
                    getEventsWithInDistance()
                } else {
                    context?.let {
                        showAlertDialog("Please Note",it,"please enter distance")
                    }
                }
                return@setOnEditorActionListener true
            }

            false
        }

        binding?.bottomSheetLayout?.setMapRadius?.setOnClickListener {
            if (!TextUtils.isEmpty(distanceInMile)) {
                getEventsWithInDistance()
            } else {
                context?.let {
                    showAlertDialog("Please Note",it,"please enter distance")
                }
            }
        }

        binding?.getMyLocation?.setOnClickListener {
            getMyLocation()
        }

        binding?.sliderButton?.setOnClickListener {
            animateEventsListVisibility()
        }
    }

    private fun getEventsWithInDistance() {
        drawCircle()
        val latLng = if (latitude != null && longitude != null) LatLng(latitude?.toDouble()!!,longitude?.toDouble()!!)
        else LatLng(lastLocation?.latitude!!, lastLocation?.longitude!!)
        if (!isNearEventsCalled) nearestEvents(latLng)
        binding?.bottomSheetLayout?.mapRadius?.hideKeyboard()
        distanceInMile = binding?.bottomSheetLayout?.mapRadius?.text.toString().trim()
//        if (lastLocation != null) {
//            distanceInMile = binding?.bottomSheetLayout?.mapRadius?.text.toString().trim()
//            lastLocation?.longitude?.let { latitude ->
//                lastLocation?.latitude?.let { longitude -> LatLng(longitude, latitude) }
//            }?.let { nearestEvents(it) }
//        }
    }

    /**
     * Expanding Recycler layout view transition animate
     * Handling click button rotational animation
     * */
    private fun animateEventsListVisibility(){
        val rotateAnimation: RotateAnimation?
        val position: Float

        binding?.linearLayout?.let { view ->
            if (!view.isVisible) {
                rotateAnimation = getRotationAnimation(0f, 180f)
                position = 180f
            } else {
                rotateAnimation = getRotationAnimation(180f, 360f)
                position = 360f
            }
            rotateAnimation.duration = 500
            binding?.sliderIcon?.startAnimation(rotateAnimation)
            rotateAnimation.setAnimationListener(object : AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    TransitionManager.beginDelayedTransition(
                        view,
                        AutoTransition()
                    )
                    TransitionManager.beginDelayedTransition(
                        binding?.sliderButton!!,
                        AutoTransition()
                    )
                    view.isVisible = !view.isVisible
                }
                override fun onAnimationEnd(animation: Animation?) {
                   binding?.sliderIcon?.rotation = position
                }
                override fun onAnimationRepeat(animation: Animation?) {} })
        }
    }

    private fun getRotationAnimation(frmDegrees: Float, toDegrees: Float): RotateAnimation {
        return RotateAnimation(
            frmDegrees, toDegrees,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
    }

    private fun drawCircle() {
        val midLatLng = mMap.cameraPosition.target
        mMap.clear()
        distanceInMile?.toDouble()?.let {
            currentLatLng?.let { it1 ->
                CircleOptions()
                    .center(it1)
                    .radius(it)
                    .strokeWidth(1.0f)
                    .strokeColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                    .fillColor(ContextCompat.getColor(requireContext(), R.color.colorMapRadiusCircle))
            }
        }?.let { mMap.addCircle(it) }
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16f))
    }

    private fun getMyLocation() {
        if (lastLocation != null) {
            val latLng = lastLocation?.longitude?.let { longitude ->
                lastLocation?.latitude?.let { latitude ->
                LatLng(latitude, longitude) } }
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16f)
            mMap.animateCamera(cameraUpdate)
        }
    }

    private fun initiateView() {
        nearEvent = GetNearEvents()
        if (!Places.isInitialized()){
            Places.initialize(
                requireContext().applicationContext, resources.getString(R.string.google_maps_key))
        }
        fields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )

        searchEvent = arguments?.getSerializable(ARG_SERIALIZABLE) as SearchByEvent
        //Log.e("TAG", "searchEvent: $searchEvent")

        BottomSheetBehavior.from(binding?.bottomSheetLayout?.bottomSheet!!).apply {
            peekHeight = 200
            this.state = BottomSheetBehavior.STATE_EXPANDED
        }
        supportMapFragment = childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment
        supportMapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        nearEventsAdapter = NearEventsAdapter(fragmentCallback = object : FragmentCallback {
            override fun onResult(param1: Any?, param2: Any?, param3: Any?) {
                val bundle = bundleOf(
                    ARG_OBJECTS to "Near Events",
                    ARG_SERIALIZABLE to param1 as? NearEventsData
                )
                findNavController().navigate(R.id.action_searchFragment_to_eventsInfoFragment, bundle)
            }
        })
        binding?.eventsRecyclerView?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = nearEventsAdapter
        }

        lifecycleScope.launch(Dispatchers.Main) {
            //Get Near Events
            val currentLatLong = if (latitude != null && longitude != null) LatLng(latitude?.toDouble()!!, longitude?.toDouble()!!)
            else SessionManager.instance.getUserLatLng()
            if (!isNearEventsCalled) nearestEvents(currentLatLong)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = false

        /*val locationButton = (supportMapFragment.view?.findViewById<View>(Integer.parseInt("1"))?.parent as View).findViewById<View>(Integer.parseInt("2"))
        val rlp =  locationButton.layoutParams as RelativeLayout.LayoutParams
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        rlp.setMargins(0, 0, 30, 30)*/

        lifecycleScope.launch(Dispatchers.Main) {
            setUpMap()
        }
    }

    private fun setUpMap() {

        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQ_LOCATION)
            return
        }
        mMap.isMyLocationEnabled = true
        mMap.setOnCameraMoveListener(this)
        mMap.setOnCameraMoveStartedListener(this)
        mMap.setOnCameraIdleListener(this)
        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->

            if (location != null){
                lastLocation = location

                val lat = if (nearEvent.lat != null) nearEvent.lat
                else SessionManager.instance.sPreference.getString(USER_LATITUDE, "")
                val lon = if (nearEvent.lon != null) nearEvent.lon
                else SessionManager.instance.sPreference.getString(USER_LONGITUDE, "")
                //Log.e("TAG", "lat: $lat  lon: $lon")
                //val currentLatLong = LatLng(location.latitude, location.longitude)
                
                val currentLatLong = lon?.toDouble()?.let { lat?.toDouble()
                    ?.let { it1 -> LatLng(it1, it) } }
                setLocationAddress(currentLatLong)
                //Log.e("TAG", "setupMap:")
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 16f))
                
                //Get Near Events
                //nearestEvents(currentLatLong)
            }
        }
    }

    private var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val place = Autocomplete.getPlaceFromIntent(data!!)
            binding?.addressSearchView?.setText(place.address)

            try {

                val addresses = geocoder.getFromLocation(
                    place.latLng?.latitude!!,
                    place.latLng?.longitude!!,
                    1
                )
                val address = addresses[0]
                country = address.countryName
                state = address.adminArea
                city = address.locality
                zip = address.postalCode
                latitude = address.latitude.toString()
                longitude = address.longitude.toString()

            }catch (e: Exception){e.printStackTrace()}

            //setLocationAddress(place.latLng!!)
            mMap.clear()
            if (!isNearEventsCalled) nearestEvents(place.latLng!!)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(place.latLng!!))
        }
        else if (result.resultCode == RESULT_ERROR){
            val status = Autocomplete.getStatusFromIntent(result.data!!)
        }
    }

    private fun setLocationAddress(currentLatLong: LatLng?) {
        geocoder = Geocoder(requireContext(), Locale.getDefault())

        val addresses: List<Address>?
        var addressLine: String? = null

        try {

            addresses = currentLatLong?.longitude?.let {
                geocoder.getFromLocation(
                    currentLatLong.latitude,
                    it,
                    1
                )
            }
            addressLine = addresses?.firstOrNull()?.getAddressLine(0)

            val address = addresses?.firstOrNull()
            country = address?.countryName
            state = address?.adminArea
            city = address?.locality
            zip = address?.postalCode
            latitude = address?.latitude.toString()
            longitude = address?.longitude.toString()

        }catch (e: Exception){e.printStackTrace()}

        if (addressLine != null){
            placeMarkerOnMap(currentLatLong, addressLine)
            //Log.e("TAG", "called latLng: $currentLatLong")
            binding?.addressSearchView?.setText(addressLine, true)
        }
    }

    private fun placeMarkerOnMap(currentLatLong: LatLng?, addressLine: String) {
        currentLatLong?.let {
            if (currentmarker == null){
                val markerOptions = MarkerOptions().position(currentLatLong)
                markerOptions.title(addressLine)
                markerOptions.icon(bitmapDescriptor())
                currentmarker = mMap.addMarker(markerOptions)

            }else{
                currentmarker?.title = addressLine
                currentmarker?.position = currentLatLong
            }
        }
    }

    private fun bitmapDescriptor(): BitmapDescriptor {
        val vectorDrawable = context?.let {
            ContextCompat.getDrawable(it, R.drawable.haystack_logo)
        }
        vectorDrawable?.setBounds(
            0, 0, 42, 42)
        val bitmap = Bitmap.createBitmap(
            42, 42, Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        vectorDrawable?.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onMarkerClick(marker: Marker) = false

    override fun onResume() {
        super.onResume()
        (activity as MainMenuActivity).hideBottomNav()
        //nearestEvents()
    }

    private fun nearestEvents(currentLatLong: LatLng) {
        isNearEventsCalled = true
        binding?.sliderButton?.isVisible = false
        binding?.linearLayout?.isVisible = false

        //nearEvent.deviceType = DEVICE_TYPE
        nearEvent.lat = currentLatLong.latitude.toString()
        nearEvent.lon = currentLatLong.longitude.toString()
        nearEvent.id = SessionManager.instance.getUserId()
        nearEvent.category = searchEvent?.category!!
        //nearEvent.distanceInMile = binding.bottomSheetLayout.mapRadius.text.toString().trim()
        nearEvent.nationWide = nationWide!!
        nearEvent.distanceInMile = distanceInMile!!
        nearEvent.currentDate = getCurrentDate()
        nearEvent.endTime = getCurrentTime()

        //Log.e("TAG", "nearEvent: $nearEvent")

        lifecycleScope.launch(Dispatchers.Main) {
            context?.let { ProgressCaller.showProgressDialog(it) }
            Repository.getNearEvents(nearEvent).enqueue(object : Callback<NearEvents>{
                override fun onResponse(call: Call<NearEvents>, response: Response<NearEvents>) {

                    try {

                        if (response.isSuccessful){
                            if (response.body()?.status == "1"){
                                response.body()?.data?.let { data ->
                                    listLatLng.clear()
                                    for (item in data){
                                        listLatLng.add(
                                            MapMarkers(
                                                latlng = LatLng(item.latitude.toDouble(), item.longitude.toDouble()),
                                                event = item.event_name
                                            )
                                        )
                                        //Log.e("TAG", "response: lat->${item.latitude}, lon->${item.longitude}")
                                    }
                                    nearEventsList.clear()
                                    nearEventsList.addAll(data)
                                    nearEventsAdapter.update(nearEventsList)
                                    setMarkers(listLatLng)
                                    binding?.sliderButton?.isVisible = true
                                    binding?.sliderIcon?.rotation = 360f
                                }
                            }else{
                                //showAlertDialog("No Events", requireContext(), response.body()?.message!!)
                                //showSnackBar(binding?.constraintCoordinatorLayout, response.body()?.message!!)
                                context?.let { Toast.makeText(
                                    it,response.body()?.message,Toast.LENGTH_SHORT).show() }
                                binding?.sliderButton?.isVisible = false
                                binding?.sliderIcon?.rotation = 360f
                            }
                            ProgressCaller.hideProgressDialog()
                        }

                    }catch (e: Exception){e.printStackTrace()}
                    ProgressCaller.hideProgressDialog()
                    isNearEventsCalled = false
                }

                override fun onFailure(call: Call<NearEvents>, t: Throwable) {
                    ProgressCaller.hideProgressDialog()
                    context?.let { context ->
                        Toast.makeText(
                            context, t.message, Toast.LENGTH_SHORT).show()
                    }
                    isNearEventsCalled = false
                    binding?.sliderButton?.isVisible = false
                    binding?.sliderIcon?.rotation = 360f
                }

            })
        }
    }

    private fun setMarkers(listLatLng: ArrayList<MapMarkers>) {
        listLatLng.forEach {
            //Log.e("TAG", "latLng: $it")
            mMap.addMarker(MarkerOptions()
                .position(it.latlng)
                .title("${it.event}, \n ${getAddress(it.latlng)}")
                .icon(bitmapDescriptor())
            )
            //mMap.animateCamera(CameraUpdateFactory.zoomTo(16f))
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(it.latlng))
        }
    }

    override fun onLocationChanged(location: Location) {
        lastLocation = location
        var latLng = LatLng(location.latitude, location.longitude)
        setLocationAddress(latLng)
        //Log.e("TAG", "locationChanged:")

        mMap.setOnCameraIdleListener {
            if (currentmarker != null){
                currentmarker?.remove()
                latLng = currentmarker?.position!!
                setLocationAddress(latLng)
                //Log.e("TAG", "cameraIdleMap:")
            }
        }
    }

    override fun onCameraMove() {}

    override fun onCameraIdle() {
        try {

            currentLatLng = LatLng(
                mMap.cameraPosition.target.latitude,
                mMap.cameraPosition.target.longitude
            )

            //Log.e("TAG", "onCameraIdle:")
            //setLocationAddress(currentLatLong)

        }
        catch (e: IOException) {e.printStackTrace()}
        catch (e: IndexOutOfBoundsException) {e.printStackTrace()}
    }

    override fun onCameraMoveStarted(p0: Int) {}

    private fun getAddress(latLng: LatLng): String{
        geocoder = Geocoder(requireContext(), Locale.getDefault())

        var addresses: List<Address>? = null
        var addressLine: String? = null

        try {

            addresses = geocoder.getFromLocation(
                latLng.latitude,
                latLng.longitude,
                1
            )
            addressLine = addresses[0].getAddressLine(0)

        }catch (e: Exception){e.printStackTrace()}

        return addressLine!!
    }

}

