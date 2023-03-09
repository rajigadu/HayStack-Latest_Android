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
import android.view.ViewGroup
import android.view.animation.Animation
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.databinding.FragmentManualMapSearchBinding
import com.haystackevents.app.`in`.manager.SessionManager
import com.haystackevents.app.`in`.network.repository.Repository
import com.haystackevents.app.`in`.network.response.countries.Countries
import com.haystackevents.app.`in`.network.response.near_events.NearEvents
import com.haystackevents.app.`in`.network.response.near_events.NearEventsData
import com.haystackevents.app.`in`.network.response.post_data.GetNearEvents
import com.haystackevents.app.`in`.network.response.search_events.SearchByEvent
import com.haystackevents.app.`in`.network.response.states.States
import com.haystackevents.app.`in`.utils.AppConstants
import com.haystackevents.app.`in`.utils.Extensions
import com.haystackevents.app.`in`.utils.Extensions.getCurrentDate
import com.haystackevents.app.`in`.utils.Extensions.getCurrentTime
import com.haystackevents.app.`in`.utils.Extensions.hideKeyboard
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

/**
 * Create by Sirumalayil on 28-02-2023.
 */

@Suppress("DEPRECATION")
class ManualSearchMapScreen: Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    LocationListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraIdleListener,
    GoogleMap.OnCameraMoveStartedListener {

    private lateinit var supportMapFragment: SupportMapFragment
    private var lastLocation: Location? = null
    private lateinit var nearEventsAdapter: NearEventsAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var binding: FragmentManualMapSearchBinding? = null
    private lateinit var mMap: GoogleMap
    private lateinit var geocoder: Geocoder
    private var currentmarker: Marker? = null
    private var fields: List<Place.Field>? = null
    private var nearEventsList = arrayListOf<NearEventsData>()

    private var searchEvent: SearchByEvent? = null
    private var nationWide: String? = "0"
    private var distanceInMile: String? = "0"
    private var nearEvent: GetNearEvents? = null
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

    private var selectedCountry: String? = "United States"
    private var listStates = arrayListOf<String>()

    private var listCountries = arrayListOf<String>()

    private var selectedState = ""

    data class MapMarkers(
        val latlng: LatLng,
        val event: String)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentManualMapSearchBinding.inflate(layoutInflater)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getCountriesList()
        initiateView()

        clickListeners()

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun clickListeners() {
        binding?.linearLayout?.layoutTransition?.enableTransitionType(LayoutTransition.CHANGING) /**Layout transition enable here*/
        binding?.sliderButton?.layoutTransition?.enableTransitionType(LayoutTransition.CHANGING) /**Layout transition enable here*/
        binding?.manualSearchView?.layoutTransition?.enableTransitionType(LayoutTransition.CHANGING) /**Layout transition enable here*/

        binding?.toolbarSearch?.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding?.manualSearchView?.setOnClickListener {
            showOrHideManualSearchView()
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

            val bundle = bundleOf(AppConstants.ARG_SERIALIZABLE to searchEvent)
            findNavController().navigate(R.id.action_manualSearchMapScreen_to_dateRangeFragment, bundle)
        }

        binding?.bottomSheetLayout?.btnManualSearch?.isVisible = false

        binding?.bottomSheetLayout?.checkNationWide?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                nationWide = "1"
                distanceInMile = "0"
                binding?.bottomSheetLayout?.mapRadius?.setText("0")
                binding?.bottomSheetLayout?.layoutMapRadius?.visibility = View.GONE
            }
            else {
                binding?.bottomSheetLayout?.layoutMapRadius?.visibility = View.VISIBLE
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
                        Extensions.showAlertDialog("Please Note", it, "please enter distance")
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
                    Extensions.showAlertDialog("Please Note", it, "please enter distance")
                }
            }
        }

        binding?.getMyLocation?.setOnClickListener {
            getMyLocation()
        }

        binding?.sliderButton?.setOnClickListener {
            animateEventsListVisibility()
        }

        binding?.btnContinue?.setOnClickListener {
            if (validated()){
                showOrHideManualSearchView()
                getEventLatLong()
                //val bundle = bundleOf(AppConstants.ARG_SERIALIZABLE to searchEvent)
                //findNavController().navigate(R.id.action_manualSearch_to_dateRangeFragment, bundle)
            }
        }

        binding?.inputCountry?.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action){
                MotionEvent.ACTION_UP -> {
                    if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                        return@setOnTouchListener false
                    }
                    lastClickTime = SystemClock.elapsedRealtime()
                    showCountriesListDialogView()
                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }
        }

        binding?.inputState?.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action){
                MotionEvent.ACTION_UP -> {
                    if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                        return@setOnTouchListener false
                    }
                    lastClickTime = SystemClock.elapsedRealtime()
                    showStatesListDialog()
                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }
        }
    }

    private fun showOrHideManualSearchView() {
        binding?.manualSearchLayout?.let { view ->
            TransitionManager.beginDelayedTransition(
                view,
                AutoTransition()
            )
            view.isVisible = !view.isVisible
            if (view.isVisible) {
                binding?.bottomSheetLayout?.bottomSheet?.let { bottomSheet ->

                }
            }
        }
    }

    private fun getEventLatLong() {
        val geoCoder = Geocoder(requireContext())
        val listAddress: List<Address>
        val locationName = searchEvent?.city + "," + searchEvent?.city + "," + searchEvent?.state +
                "," + searchEvent?.zipcode

        try {
            listAddress = geoCoder.getFromLocationName(locationName, 5)
            if (listAddress != null){
                val location: Address = listAddress[0]
                country = location.countryName
                state = location.adminArea
                city = location.locality
                zip = location.postalCode
                latitude = location.latitude.toString()
                longitude = location.longitude.toString()

                searchEvent?.latitude = location.latitude.toString()
                searchEvent?.longitude = location.longitude.toString()
                if (!isNearEventsCalled) nearestEvents(LatLng(location.latitude, location.longitude))
                mMap.clear()
                mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(location.latitude, location.longitude)))
            }

        }catch (e: java.lang.Exception){e.printStackTrace()}
    }

    private fun validated(): Boolean {
        searchEvent?.country = binding?.inputCountry?.text.toString().trim()
        searchEvent?.state = binding?.inputState?.text.toString().trim()
        searchEvent?.zipcode = binding?.inputZipCode?.text.toString().trim()
        searchEvent?.city = binding?.inputCity?.text?.toString()?.trim()
        searchEvent?.address = binding?.inputAddress?.text?.toString()?.trim()

        when {
            searchEvent?.country?.isEmpty() == true -> {
                binding?.inputCountry?.error = "Select Your Country"
                binding?.inputCountry?.requestFocus()
                return false
            }

            searchEvent?.state?.isEmpty() == true -> {
                binding?.inputState?.error = "Select Your State"
                binding?.inputState?.requestFocus()
                return false
            }

//            searchEvent?.city?.isEmpty() == true -> {
//                longSnackBar("Enter Your City", binding?.constraintManualSearch)
//                return false
//            }
//
//            searchEvent?.zipcode?.isEmpty() == true -> {
//                longSnackBar("Enter Zip code", binding?.constraintManualSearch)
//                return false
//            }

            else -> return true
        }

    }

    private fun showStatesListDialog() {
        var array = arrayOf<String>()
        array = listStates.toArray(array)

        MaterialAlertDialogBuilder(requireContext(), R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle("Select Event State")
            .setCancelable(false)
            .setPositiveButton("Ok"){ dialog, which ->
                binding?.inputState?.setText(selectedState)
            }
            .setSingleChoiceItems(array,-1){ dialog, which ->
                selectedState = array[which]
            }
            .show()
    }

    private fun getCountriesList() {
        //context?.let { ProgressCaller.showProgressDialog(it) }
        Repository.getAllCountries().enqueue(object : Callback<Countries>{
            override fun onResponse(call: Call<Countries>, response: Response<Countries>) {
                try {

                    if (response.isSuccessful){
                        if (response.body()?.status == "1"){
                            if (response.body()?.data?.size!! > 0){
                                listCountries.clear()
                                for (elements in response.body()?.data!!){
                                    listCountries.add(elements.name)
                                }
                            }
                        }
                    }
                    ProgressCaller.hideProgressDialog()

                }catch (e: Exception){e.printStackTrace()}
            }

            override fun onFailure(call: Call<Countries>, t: Throwable) {
                context?.let { Toast.makeText(
                    it,t.message, Toast.LENGTH_SHORT).show() }
                ProgressCaller.hideProgressDialog()
            }

        })
    }

    private fun showCountriesListDialogView() {
        var array = arrayOf<String>()
        array = listCountries.toArray(array)

        MaterialAlertDialogBuilder(requireContext(), R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle("Select Event Country")
            .setCancelable(false)
            .setPositiveButton("Ok"){ dialog, which ->
                context?.let { ProgressCaller.showProgressDialog(it) }
                getStatesList()
                binding?.inputCountry?.setText(selectedCountry)
                binding?.inputState?.setText("")
                selectedState = ""
            }
            .setSingleChoiceItems(array,-1){ dialog, which ->
                selectedCountry = array[which]
            }
            .show()
    }

    private fun getStatesList() {
        Repository.getAllStatesOfTheCountry(selectedCountry!!).enqueue(
            object : Callback<States> {
                override fun onResponse(call: Call<States>, response: Response<States>) {
                    try {

                        if (response.isSuccessful){
                            if (response.body()?.status == "1"){
                                if (response.body()?.data?.size!! > 0){
                                    listStates.clear()
                                    for (item in response.body()?.data!!){
                                        listStates.add(item.name)
                                    }
                                }
                            }
                        }

                    }catch (e: Exception){e.printStackTrace()}
                    ProgressCaller.hideProgressDialog()
                }

                override fun onFailure(call: Call<States>, t: Throwable) {
                    context?.let { Toast.makeText(
                        it,t.message, Toast.LENGTH_SHORT).show() }
                    ProgressCaller.hideProgressDialog()
                }

            })
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
            rotateAnimation.setAnimationListener(object : Animation.AnimationListener {
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

        searchEvent = arguments?.getSerializable(AppConstants.ARG_SERIALIZABLE) as SearchByEvent
        binding?.inputCountry?.setText(selectedCountry)
        //Log.e("TAG", "searchEvent: $searchEvent")

        BottomSheetBehavior.from(binding?.bottomSheetLayout?.bottomSheet!!).apply {
            peekHeight = 200
            this.state = BottomSheetBehavior.STATE_EXPANDED
        }
        supportMapFragment = childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment
        supportMapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        nearEventsAdapter = NearEventsAdapter(fragmentCallback = object: FragmentCallback {
            override fun onResult(param1: Any?, param2: Any?, param3: Any?) {
                val bundle = bundleOf(
                    AppConstants.ARG_OBJECTS to "Near Events",
                    AppConstants.ARG_SERIALIZABLE to param1 as? NearEventsData
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
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION),
                AppConstants.PERMISSION_REQ_LOCATION
            )
            return
        }
        mMap.isMyLocationEnabled = true
        mMap.setOnCameraMoveListener(this)
        mMap.setOnCameraMoveStartedListener(this)
        mMap.setOnCameraIdleListener(this)
        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->

            if (location != null){
                lastLocation = location

                val lat = if (nearEvent?.lat != null) nearEvent?.lat
                else SessionManager.instance.sPreference.getString(AppConstants.USER_LATITUDE, "")
                val lon = if (nearEvent?.lon != null) nearEvent?.lon
                else SessionManager.instance.sPreference.getString(AppConstants.USER_LONGITUDE, "")
                //Log.e("TAG", "lat: $lat  lon: $lon")
                //val currentLatLong = LatLng(location.latitude, location.longitude)

                val currentLatLong = LatLng(lat!!.toDouble(), lon!!.toDouble())
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
        else if (result.resultCode == AutocompleteActivity.RESULT_ERROR){
            val status = Autocomplete.getStatusFromIntent(result.data!!)
        }
    }

    private fun setLocationAddress(currentLatLong: LatLng) {
        geocoder = Geocoder(requireContext(), Locale.getDefault())

        val addresses: List<Address>?
        var addressLine: String? = null

        try {

            addresses = geocoder.getFromLocation(
                currentLatLong.latitude,
                currentLatLong.longitude,
                1
            )
            addressLine = addresses[0].getAddressLine(0)

            val address = addresses[0]
            country = address.countryName
            state = address.adminArea
            city = address.locality
            zip = address.postalCode
            latitude = address.latitude.toString()
            longitude = address.longitude.toString()

        }catch (e: Exception){e.printStackTrace()}

        if (addressLine != null){
            placeMarkerOnMap(currentLatLong, addressLine)
            //Log.e("TAG", "called latLng: $currentLatLong")
        }
    }

    private fun placeMarkerOnMap(currentLatLong: LatLng, addressLine: String) {

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
        getStatesList()
    }

    private fun nearestEvents(currentLatLong: LatLng?) {
        isNearEventsCalled = true
        binding?.sliderButton?.isVisible = false
        binding?.linearLayout?.isVisible = false

        //nearEvent.deviceType = DEVICE_TYPE
        nearEvent?.lat = currentLatLong?.latitude.toString()
        nearEvent?.lon = currentLatLong?.longitude.toString()
        nearEvent?.id = SessionManager.instance.getUserId()
        nearEvent?.category = searchEvent?.category!!
        //nearEvent.distanceInMile = binding.bottomSheetLayout.mapRadius.text.toString().trim()
        nearEvent?.nationWide = nationWide!!
        nearEvent?.distanceInMile = distanceInMile!!
        nearEvent?.currentDate = getCurrentDate()
        nearEvent?.endTime = getCurrentTime()

        //Log.e("TAG", "nearEvent: $nearEvent")

        lifecycleScope.launch(Dispatchers.Main) {
            context?.let { ProgressCaller.showProgressDialog(it) }
            Repository.getNearEvents(nearEvent).enqueue(object : Callback<NearEvents> {
                @SuppressLint("NotifyDataSetChanged")
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
                                    it,response.body()?.message, Toast.LENGTH_SHORT).show() }
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
            mMap.addMarker(
                MarkerOptions()
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