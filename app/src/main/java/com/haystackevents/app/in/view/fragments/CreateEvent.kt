package com.haystackevents.app.`in`.view.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.databinding.FragmentCreateEventBinding
import com.haystackevents.app.`in`.manager.SessionManager
import com.haystackevents.app.`in`.network.repository.Repository
import com.haystackevents.app.`in`.network.response.countries.Countries
import com.haystackevents.app.`in`.network.response.event.Event
import com.haystackevents.app.`in`.network.response.states.States
import com.haystackevents.app.`in`.utils.AppConstants.ARG_OBJECTS
import com.haystackevents.app.`in`.utils.AppConstants.ARG_SERIALIZABLE
import com.haystackevents.app.`in`.utils.Extensions
import com.haystackevents.app.`in`.utils.Extensions.convertedDateFormat
import com.haystackevents.app.`in`.utils.Extensions.showErrorResponse
import com.haystackevents.app.`in`.utils.Extensions.showSnackBar
import com.haystackevents.app.`in`.view.activity.MainMenuActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class CreateEvent: Fragment(),MultiplePermissionsListener {

    private var binding: FragmentCreateEventBinding? = null
    private lateinit var bottomSheet: BottomSheetDialog
    private lateinit var events: Event
    private var lastClickTime: Long = 0
    private var listCountries = arrayListOf<String>()
    private var listStates = arrayListOf<String>()
    private var selectedCountry: String? = "United States"
    private var selectedState: String? = ""
    private var selectedImageUri: Uri? = null

    private val permissionCamera = listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateEventBinding.inflate(layoutInflater)
        return binding?.root
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initiateView()

        clickListeners()

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun clickListeners() {

        binding?.btnCreateEvent?.setOnClickListener {
            if (validated()) {
                if (selectedImageUri != null){
                    //showSnackBar(binding.constraintCreateEvent, "Please select event image")
                    //return@setOnClickListener
                    events.image = selectedImageUri.toString()
                }

                val bundle = bundleOf(
                    ARG_SERIALIZABLE to events,
                    ARG_OBJECTS to "1"
                )
                findNavController().navigate(R.id.action_createEvent_to_categoriesFragment, bundle)
            }else{
                showSnackBar(binding?.constraintCreateEvent, "Please fill all fields")
            }
            Log.e("TAG", "event: $events")
        }

        binding?.inputStartDate?.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action){
                ACTION_UP -> {
                    if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                        return@setOnTouchListener false
                    }
                    lastClickTime = SystemClock.elapsedRealtime()
                    showDatePickerDialog("Select Event Start Date")
                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }
        }

        binding?.inputEndDate?.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action){
                ACTION_UP -> {
                    if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                        return@setOnTouchListener false
                    }
                    lastClickTime = SystemClock.elapsedRealtime()
                    selectEndDate("Select Event End Date")
                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }
        }

        binding?.startTime?.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action){
                ACTION_UP -> {
                    if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                        return@setOnTouchListener false
                    }
                    lastClickTime = SystemClock.elapsedRealtime()
                    showTimePickerDialog("Select Event Start Time")
                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }
        }

        binding?.endTime?.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action){
                ACTION_UP -> {
                    if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                        return@setOnTouchListener false
                    }
                    lastClickTime = SystemClock.elapsedRealtime()
                    showTimePickerDialog("Select Event End Time")
                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }
        }

        binding?.inputCountry?.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action){
                ACTION_UP -> {
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
                ACTION_UP -> {
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

        binding?.eventImage?.setOnClickListener {
            Dexter.withContext(requireContext())
                .withPermissions(permissionCamera)
                .withListener(this)
                .onSameThread()
                .check()
        }

    }

    private fun initiateView() {

        events = Event()

        lifecycleScope.launch(Dispatchers.Main) {
            getCountriesList()
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

    private fun showCountriesListDialogView() {
        var array = arrayOf<String>()
        array = listCountries.toArray(array)

        MaterialAlertDialogBuilder(requireContext(), R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle("Select Event Country")
            .setCancelable(false)
            .setPositiveButton("Ok"){ dialog, which ->
                getStatesList()
                binding?.inputCountry?.setText(selectedCountry)
                binding?.inputState?.setText("")
                selectedState = null
            }
            .setSingleChoiceItems(array,-1){ dialog, which ->
                selectedCountry = array[which]
            }
            .show()
    }

    private fun getStatesList() {
        binding?.progressCardView?.visibility = VISIBLE
        Repository.getAllStatesOfTheCountry(selectedCountry!!).enqueue(
            object : Callback<States>{
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
                    binding?.progressCardView?.visibility = INVISIBLE
                }

                override fun onFailure(call: Call<States>, t: Throwable) {
                    showErrorResponse(t, binding?.constraintCreateEvent)
                    binding?.progressCardView?.visibility = INVISIBLE
                }

            })
    }

    private fun getCountriesList() {
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

                }catch (e: Exception){e.printStackTrace()}
            }

            override fun onFailure(call: Call<Countries>, t: Throwable) {
                showErrorResponse(t, binding?.constraintCreateEvent)
            }

        })
    }

    private fun showTimePickerDialog(timePickerTitle: String) {
        val timePicker = MaterialTimePicker.Builder()
            .setTheme(R.style.TimePickerTheme)
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(60)
            .setTitleText(timePickerTitle)
            .build()
        timePicker.isCancelable = false
        timePicker.addOnPositiveButtonClickListener {
            var timeState = "AM"
            if (timePicker.hour > 11) timeState = "PM"

            var hour = timePicker.hour.toString()
            if (timePicker.hour > 12) hour = (timePicker.hour -12).toString()
            var minute = timePicker.minute.toString()
            if (timePicker.hour.toString().length == 1) hour = "0$hour"
            if (minute.length == 1) minute = "0$minute"
            if (hour == "00") hour = "12"

            val selectedTime = "$hour:$minute $timeState"

            if (timePickerTitle == "Select Event Start Time"){
                events.starttime = selectedTime
                binding?.startTime?.setText(selectedTime)
            }else{
                events.endtime = selectedTime
                binding?.endTime?.setText(selectedTime)
            }
        }
        timePicker.addOnNegativeButtonClickListener {
            timePicker.dismiss()
        }
        timePicker.show(requireActivity().supportFragmentManager,
            context?.resources?.getString(R.string.time_picker))
    }

    private fun showDatePickerDialog(datePickerTitle: String) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTheme(R.style.DatePickerTheme)
            //.setCalendarConstraints(constraintsBuilder.build())
            .setTitleText(datePickerTitle)
            .build()

        datePicker.isCancelable = false
        datePicker.addOnPositiveButtonClickListener {

            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.time = Date(it)

            var month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)
            var day = calendar.get(Calendar.DAY_OF_MONTH)

            if (month < 10) month = "0$month".toInt()
            if (day < 10) day = "0$day".toInt()

            val selectedDate = "$month-$day-$year"

            events.startdate = selectedDate
            binding?.inputStartDate?.setText(datePicker.headerText)

        }

        datePicker.addOnNegativeButtonClickListener {
            datePicker.dismiss()
        }

        datePicker.show(requireActivity().supportFragmentManager,
            context?.resources?.getString(R.string.date_picker)
        )

    }

    private fun selectEndDate(datePickerTitle: String) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTheme(R.style.DatePickerTheme)
            .setCalendarConstraints(constraintsBuilder.build())
            .setTitleText(datePickerTitle)
            .build()

        datePicker.isCancelable = false
        datePicker.addOnPositiveButtonClickListener {

            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.time = Date(it)

            var month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)
            var day = calendar.get(Calendar.DAY_OF_MONTH)

            if (month < 10) month = "0$month".toInt()
            if (day < 10) day = "0$day".toInt()

            val selectedDate = "$month-$day-$year"

            events.enddate = selectedDate
            binding?.inputEndDate?.setText(datePicker.headerText)

        }

        datePicker.addOnNegativeButtonClickListener {
            datePicker.dismiss()
        }

        datePicker.show(requireActivity().supportFragmentManager,
            context?.resources?.getString(R.string.date_picker)
        )

    }

    private val constraintsBuilder =
        CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())

    override fun onResume() {
        super.onResume()
        (activity as MainMenuActivity).updateBottomNavChange(4)
        (activity as MainMenuActivity).showBottomNav()
    }

    private fun validated(): Boolean {
        events.id = SessionManager.instance.getUserId()
        events.event_name = binding?.inputEventName?.text.toString().trim()
        events.event_description = binding?.eventDescription?.text.toString().trim()
        events.streetaddress = binding?.inputStreetAddress?.text.toString().trim()
        events.city = binding?.inputCity?.text.toString().trim()
        events.state = selectedState!!
        events.country = selectedCountry!!
        events.zipcode = binding?.inputZipCode?.text.toString().trim()
        events.hostname = binding?.inputHostName?.text.toString().trim()
        events.contactinfo = binding?.inputContactInfoOne?.text.toString().trim()
        events.starttime = binding?.startTime?.text.toString().trim()
        events.endtime = binding?.endTime?.text.toString().trim()
        events.startdate = convertedDateFormat(binding?.inputStartDate?.text.toString().trim())
        events.enddate = convertedDateFormat(binding?.inputEndDate?.text.toString().trim())
        //events.image = selectedImageUri!!

        when {
            events.event_name.isEmpty() -> {
                binding?.inputEventName?.requestFocus()
                binding?.inputEventName?.error = "Enter Event Name"
                return false
            }
            events.event_description.isEmpty() -> {
                binding?.eventDescription?.requestFocus()
                binding?.eventDescription?.error = "Enter Event Description"
                return false
            }
            events.streetaddress.isEmpty() -> {
                binding?.inputStreetAddress?.requestFocus()
                binding?.inputStreetAddress?.error = "Enter Street Address"
                return false
            }
            events.city.isEmpty() -> {
                binding?.inputCity?.requestFocus()
                binding?.inputCity?.error = "Enter City Name"
                return false
            }
            events.state.isEmpty() -> {
                binding?.inputState?.requestFocus()
                binding?.inputState?.error = "Enter State Name"
                return false
            }
            events.zipcode.isEmpty() -> {
                binding?.inputZipCode?.requestFocus()
                binding?.inputZipCode?.error = "Enter Zip code"
                return false
            }
            events.hostname.isEmpty() -> {
                binding?.inputHostName?.requestFocus()
                binding?.inputHostName?.error = "Enter Host Name"
                return false
            }
            events.contactinfo.isEmpty() -> {
                binding?.inputContactInfoOne?.requestFocus()
                binding?.inputContactInfoOne?.error = "Enter Contact Info"
                return false
            }
            events.starttime?.isEmpty() == true-> {
                Extensions.longSnackBar("Please select event start time", binding?.constraintCreateEvent)
                return false
            }
            events.endtime?.isEmpty() == true -> {
                Extensions.longSnackBar("Please select event end time", binding?.constraintCreateEvent)
                return false
            }
            events.startdate?.isEmpty() == true -> {
                Extensions.longSnackBar("Please select event start date", binding?.constraintCreateEvent)
                return false
            }
            events.enddate?.isEmpty() == true -> {
                Extensions.longSnackBar("Please select event end date", binding?.constraintCreateEvent)
                return false
            }

            else -> return true
        }
    }

    override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
        if (p0?.areAllPermissionsGranted()!!){
            chooseImagePickerOption()
        }
        if (p0.isAnyPermissionPermanentlyDenied){
            showPermissionAlertDialog()
        }
    }

    private fun chooseImagePickerOption() {
        bottomSheet = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(requireContext())
            .inflate(
                R.layout.image_picker_chooser_dialog_view,
                requireActivity().findViewById<ConstraintLayout>(R.id.bottom_sheet)
            )
        bottomSheet.setCancelable(false)
        bottomSheet.setContentView(view)
        bottomSheet.show()

        val btnGallery = view.findViewById<TextView>(R.id.actionGallery)
        val btnCamera = view.findViewById<TextView>(R.id.actionCamera)
        val btnCancel = view.findViewById<MaterialButton>(R.id.dialogCancel)

        btnGallery.setOnClickListener {
            openGallery()
            bottomSheet.hide()
        }

        btnCamera.setOnClickListener {
            openCamera()
            bottomSheet.hide()
        }

        btnCancel.setOnClickListener {
            bottomSheet.hide()
        }
    }

    private fun openCamera() {
        try {
            //camera intent
            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startCameraActivityResult.launch(callCameraIntent)

        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    private val startCameraActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode == Activity.RESULT_OK) {
            try {
                binding?.eventImage?.setImageBitmap(result.data?.extras!!.get("data") as Bitmap)
                selectedImageUri = getImageUri(requireContext(),
                    result.data?.extras!!.get("data") as Bitmap)
                Log.e("TAG", "data: $selectedImageUri")

            } catch (e: Exception) {
                //Log.e("TAG", "Exception: " + e.printStackTrace())
                e.printStackTrace()
            }
        }
    }

    private val startGalleryActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode == Activity.RESULT_OK &&
            result.data != null && result?.data?.data != null) {
            selectedImageUri = result.data?.data as Uri
            binding?.eventImage?.setImageURI(selectedImageUri)
        }
    }

    private fun openGallery() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
        }
        startGalleryActivityResult.launch(intent)
    }

    override fun onPermissionRationaleShouldBeShown(
        p0: MutableList<PermissionRequest>?,
        p1: PermissionToken?
    ) {
        p1!!.continuePermissionRequest()
    }

    private fun showPermissionAlertDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setCancelable(false)
            .setTitle("Need Permissions")
            .setMessage("This app needs permission to use this feature. You can grant them in app settings.")
            .setIcon(R.drawable.ic_baseline_photo_camera_24)
            .setPositiveButton("GOTO SETTINGS"){ dialogInterface, i ->
                dialogInterface.dismiss()
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts(
                        getString(R.string.package_),
                        requireActivity().packageName, null
                    )
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .setNegativeButton("Cancel"){ dialogInterface, i ->
                dialogInterface.dismiss()
            }
            .create()
            .show()
    }

    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            inContext.contentResolver,
            inImage,
            "Title",
            null
        )
        return Uri.parse(path)
    }
}