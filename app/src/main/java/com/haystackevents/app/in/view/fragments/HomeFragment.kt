package com.haystackevents.app.`in`.view.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.*
import com.haystackevents.app.`in`.databinding.FragmentHomeBinding
import com.haystackevents.app.`in`.network.repository.Repository
import com.haystackevents.app.`in`.network.response.nearest_events.NearestEventData
import com.haystackevents.app.`in`.network.response.nearest_events.NearestEvents
import com.haystackevents.app.`in`.utils.AppConstants.ARG_SERIALIZABLE
import com.haystackevents.app.`in`.utils.Extensions.getDeviceUid
import com.haystackevents.app.`in`.view.activity.MainMenuActivity
import com.haystackevents.app.`in`.view.adapters.NearestEventsListAdapter
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.manager.SessionManager
import com.haystackevents.app.`in`.utils.AppConstants.PERMISSION_REQ_LOCATION
import com.haystackevents.app.`in`.utils.Extensions.isGpsEnable
import com.haystackevents.app.`in`.utils.Extensions.showSnackBarSettings
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import com.haystackevents.app.`in`.utils.AppConstants.ARG_OBJECTS as ARG_OBJECTS1

class HomeFragment: Fragment(), MultiplePermissionsListener, NearestEventsListAdapter.NearestEventsOnClick {

    private var binding: FragmentHomeBinding? = null
    private lateinit var nearestEventsListAdapter: NearestEventsListAdapter

    private val permissionsLocation = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )
    private var geocoder: Geocoder? = null

    private var userLocation: String? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var lastLocation: Location? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var category: String? = ""
    private var searchType: String? = ""
    private var endTime: String? = ""
    private var currentDate: String? = ""
    private var listNearestEvents = arrayListOf<NearestEventData>()



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding?.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initiateView()

        onClickListener()

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun onClickListener() {
        binding?.searchView?.setOnTouchListener { view, motionEvent ->
            when(motionEvent.action){
                ACTION_UP -> {
                    val bundle = bundleOf(ARG_OBJECTS1 to "0")
                    findNavController().navigate(R.id.action_homeFragment_to_categoriesFragment, bundle)
                }
            }
            return@setOnTouchListener true
        }

        binding?.btnMyEvents?.setOnClickListener {
            val bundle = bundleOf(ARG_OBJECTS1 to 0)
            findNavController().navigate(R.id.action_homeFragment_to_myEvents, bundle)
        }

        binding?.btnInterestEvents?.setOnClickListener {
            val bundle = bundleOf(ARG_OBJECTS1 to 1)
            findNavController().navigate(R.id.action_homeFragment_to_myEvents, bundle)
        }

        binding?.btnAttendEvents?.setOnClickListener {
            val bundle = bundleOf(ARG_OBJECTS1 to 2)
            findNavController().navigate(R.id.action_homeFragment_to_myEvents, bundle)
        }

        binding?.btnInviteEvents?.setOnClickListener {
            val bundle = bundleOf(ARG_OBJECTS1 to 3)
            findNavController().navigate(R.id.action_homeFragment_to_myEvents, bundle)
        }

        binding?.refreshNearestEvents?.setColorSchemeColors(ContextCompat.getColor(
            requireContext(), R.color.colorPrimary))

        binding?.refreshNearestEvents?.setOnRefreshListener {
            nearestEvents()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initiateView() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        Dexter.withContext(requireContext())
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .withListener(this)
            .onSameThread()
            .check()


        nearestEventsListAdapter = NearestEventsListAdapter(requireContext())
        binding?.nearestEventsList?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = nearestEventsListAdapter
        }
    }

    override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
        if (p0!!.areAllPermissionsGranted()){
            getCurrentUserLocation()
        }
        else if (p0.isAnyPermissionPermanentlyDenied){
            showSnackBarSettings(
                requireContext(),
                "Permission was denied",
                "Settings",
                View.OnClickListener {
                    // Build intent that displays the App settings screen.
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts(
                        "package",
                        Build.DISPLAY, null
                    )
                    intent.data = uri
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            )
        }
    }

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {permissions ->
        val granted = permissions.entries.all {
            it.value == true
        }
        if (granted) {
            getLocationData()
        }
    }

    private fun getCurrentUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission.launch(permissionsLocation)
            //ActivityCompat.requestPermissions(requireActivity(), permissionsLocation, PERMISSION_REQ_LOCATION)
            return
        }else{
            getLocationData()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocationData() {
        fusedLocationClient?.lastLocation?.addOnSuccessListener(requireActivity()) { location ->
            lastLocation = location

            if (lastLocation == null) {
                requestNewLocationData()
            }else{
                latitude = lastLocation!!.latitude
                longitude = lastLocation!!.longitude

                Log.e("TAG", "lat: $latitude   lng: $longitude")
                SessionManager.instance.saveUserLatLong(latitude!!, longitude!!)

                if (latitude != null && longitude != null){
                    getUserAddress()
                    nearestEvents()
                }
            }
        }
    }

    private fun nearestEvents() {
        binding?.refreshNearestEvents?.isRefreshing = true
        val deviceId = getDeviceUid(requireContext())
        Repository.getNearestEvents(deviceId, latitude.toString(), longitude.toString(), category!!,
            searchType!!, currentDate!!, endTime!!).enqueue(object : Callback<NearestEvents>{
            override fun onResponse(call: Call<NearestEvents>, response: Response<NearestEvents>) {
                try {

                    if (response.isSuccessful){
                        if (response.body()?.status == "1"){

                            binding?.noEventsImgView?.visibility = INVISIBLE
                            binding?.noEventsText?.visibility = INVISIBLE

                            if (response.body()?.data != null){
                                listNearestEvents.clear()
                                listNearestEvents.addAll(response.body()?.data!!)
                                nearestEventsListAdapter.update(listNearestEvents, this@HomeFragment)
                            }

                        }else{
                            //binding?.noEventsImgView.visibility = VISIBLE
                            //binding?.noEventsText.visibility = VISIBLE
                            //longSnackBar(response.body()?.message!!, binding?.constraintHome)
                        }
                    }

                }catch (e: Exception){e.printStackTrace()}
                binding?.refreshNearestEvents?.isRefreshing = false
            }

            override fun onFailure(call: Call<NearestEvents>, t: Throwable) {
                try {
                    //if (binding?.constraintHome != null)showErrorResponse(t, binding?.constraintHome)
                }catch (e: Exception){e.printStackTrace()}

                binding?.refreshNearestEvents?.isRefreshing = false

            }

        })
    }

    @SuppressLint("SetTextI18n")
    private fun getUserAddress() {
        geocoder = Geocoder(requireContext(), Locale.getDefault())
        var addresses: List<Address>? = null

        try {
            // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            addresses = geocoder!!.getFromLocation(
                latitude!!,
                longitude!!,
                1)

            if (addresses!![0] != null) {
                val city: String = addresses[0].locality
                val state: String = addresses[0].adminArea
                val country: String = addresses[0].countryName
                val postalCode: String = addresses[0].postalCode
                val knownName: String = addresses[0].featureName // Only if available else return NULL

                binding?.userLocation?.text = "$city, $state, $country, $postalCode"
            }

        }catch (e: IOException){e.printStackTrace()}

        /*if (addresses?.isNotEmpty()!!){

            if (addresses[0].getAddressLine(0) != null){
                userLocation = addresses[0].getAddressLine(0)
            }
            if (addresses[0].getAddressLine(1) != null){
                userLocation = addresses[0].getAddressLine(0)
            }

        }*/

        //Log.e("TAG", "user location: $userLocation")

    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        // Initializing LocationRequest
        // object with appropriate methods
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 5
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        // setting LocationRequest
        // on FusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationClient!!.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()!!
        )
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation = locationResult.lastLocation
            latitude = mLastLocation.latitude
            longitude = mLastLocation.longitude
        }
    }

    override fun onPermissionRationaleShouldBeShown(
        p0: MutableList<PermissionRequest>?,
        p1: PermissionToken?
    ) {
        p1?.continuePermissionRequest()
    }

    @SuppressLint("SimpleDateFormat")
    override fun onResume() {
        super.onResume()
        (activity as MainMenuActivity).updateBottomNavChange(0)
        (activity as MainMenuActivity).showBottomNav()
        val sdf = SimpleDateFormat("EEEE,dd MMM")
        binding?.currentDate?.text = sdf.format(Date())
    }

    override fun nearestEventClick(nearEvents: NearestEventData) {
        val bundle = bundleOf(
            ARG_OBJECTS1 to "Nearest Events",
            ARG_SERIALIZABLE to nearEvents
        )
        findNavController().navigate(R.id.action_homeFragment_to_eventsInfoFragment, bundle)
    }
}