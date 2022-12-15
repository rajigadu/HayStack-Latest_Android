package com.haystackevents.app.`in`.view.fragments

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
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
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
import com.haystackevents.app.`in`.network.UpdateEvent
import com.haystackevents.app.`in`.databinding.FragmentEditEventBinding
import com.haystackevents.app.`in`.manager.SessionManager
import com.haystackevents.app.`in`.network.repository.Repository
import com.haystackevents.app.`in`.network.response.countries.Countries
import com.haystackevents.app.`in`.network.response.group_members.DefaultResponse
import com.haystackevents.app.`in`.network.response.my_events.MyEventsData
import com.haystackevents.app.`in`.network.response.states.States
import com.haystackevents.app.`in`.utils.AppConstants.ARG_SERIALIZABLE
import com.haystackevents.app.`in`.utils.Extensions.longSnackBar
import com.haystackevents.app.`in`.utils.Extensions.showAlertDialog
import com.haystackevents.app.`in`.utils.Extensions.showErrorResponse
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class EditEvents: Fragment(), MultiplePermissionsListener {

    private lateinit var binding: FragmentEditEventBinding
    private lateinit var myEvents: MyEventsData
    private lateinit var bottomSheet: BottomSheetDialog
    private var selectedState = ""
    private var selectedCountry = ""
    private lateinit var updateEvent: UpdateEvent
    private var selectedImageUri: Uri? = null
    private var lastClickTime: Long = 0
    private var listStates = arrayListOf<String>()
    private var listCountries = arrayListOf<String>()




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditEventBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateEvent = UpdateEvent()
        myEvents = arguments?.getSerializable(ARG_SERIALIZABLE) as MyEventsData
        setEditEventData()

        getCountriesList()

        clickListeners()

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun clickListeners() {

        binding.btnUpdateEvent.setOnClickListener {
            if (validated()){
                updateEvent()
            }
        }

        binding.editStartDate.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action){
                MotionEvent.ACTION_UP -> {
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

        binding.editEndDate.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action){
                MotionEvent.ACTION_UP -> {
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

        binding.editStartTime.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action){
                MotionEvent.ACTION_UP -> {
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

        binding.editEndTime.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action){
                MotionEvent.ACTION_UP -> {
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

        binding.editCountry.setOnTouchListener { view, motionEvent ->
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

        binding.editState.setOnTouchListener { view, motionEvent ->
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

        binding.toolbarEditEvent.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

    }

    private fun showStatesListDialog() {
        var array = arrayOf<String>()
        array = listStates.toArray(array)

        MaterialAlertDialogBuilder(requireContext(), R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle("Select Event State")
            .setCancelable(false)
            .setPositiveButton("Ok"){ dialog, which ->
                binding.editState.setText(selectedState)
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
                binding.editCountry.setText(selectedCountry)
            }
            .setSingleChoiceItems(array,-1){ dialog, which ->
                selectedCountry = array[which]
            }
            .show()
    }

    private fun getStatesList() {
        Repository.getAllStatesOfTheCountry(selectedCountry).enqueue(
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
                }

                override fun onFailure(call: Call<States>, t: Throwable) {
                    showErrorResponse(t, binding.constraintEditEvent)
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
                showErrorResponse(t, binding.constraintEditEvent)
            }

        })
    }

    private fun updateEvent() {
        showBottomSheet()
        Repository.updateEvent(requireContext(), updateEvent).enqueue(object : Callback<DefaultResponse>{
            override fun onResponse(
                call: Call<DefaultResponse>,
                response: Response<DefaultResponse>
            ) {
                try {

                    if (response.isSuccessful){
                        if (response.body()?.status == "1"){

                            showSuccessAlert("Event Updated", response.body()?.message!!)

                        }else{
                            showAlertDialog("Error Occurred", requireContext(), response.body()?.message)
                        }
                    }

                }catch (e: Exception){e.printStackTrace()}
                hideBottomSheet()
            }

            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                showErrorResponse(t, binding.constraintEditEvent)
                hideBottomSheet()
            }

        })
    }

    private fun validated(): Boolean {
        updateEvent.id = SessionManager.instance.getUserId()
        updateEvent.event_name = binding.editEventName.text.toString().trim()
        //updateEvent.des = binding.editEventDescription.text.toString().trim()
        updateEvent.streetaddress = binding.editStreetAddress.text.toString().trim()
        updateEvent.city = binding.editCity.text.toString().trim()
        updateEvent.state = binding.editState.text.toString().trim()
        updateEvent.country = binding.editCountry.text.toString().trim()
        updateEvent.zipcode = binding.editZipCode.text.toString().trim()
        updateEvent.hostname = binding.editHostName.text.toString().trim()
        updateEvent.contactInfo = binding.editContactInfoOne.text.toString().trim()
        updateEvent.hosttype = myEvents.hosttype
        updateEvent.startDate = binding.editStartDate.text.toString().trim()
        updateEvent.endDate = binding.editEndDate.text.toString().trim()
        updateEvent.startTime = binding.editStartTime.text.toString().trim()
        updateEvent.endTime = binding.editEndTime.text.toString().trim()
        //updateEvent.eventType = myEvents.eve
        updateEvent.latitude = myEvents.latitude
        updateEvent.longitude = myEvents.longitude
        updateEvent.category = myEvents.category
        updateEvent.eventId = myEvents.id
        updateEvent.userid = myEvents.userid

        when {

            updateEvent.event_name.isEmpty() -> {
                binding.editEventName.requestFocus()
                binding.editEventName.error = "Enter Event Name"
                return false
            }
            /*updateEvent.event_description.isEmpty() -> {
                binding.editEventDescription.requestFocus()
                binding.editEventDescription.error = "Enter Event Description"
                return false
            }*/
            updateEvent.streetaddress.isEmpty() -> {
                binding.editStreetAddress.requestFocus()
                binding.editStreetAddress.error = "Enter Street Address"
                return false
            }
            updateEvent.city.isEmpty() -> {
                binding.editCity.requestFocus()
                binding.editCity.error = "Enter City Name"
                return false
            }
            updateEvent.state.isEmpty() -> {
                longSnackBar("Please select event city", binding.constraintEditEvent)
                return false
            }
            updateEvent.zipcode.isEmpty() -> {
                binding.editZipCode.requestFocus()
                binding.editZipCode.error = "Enter Zip code"
                return false
            }
            updateEvent.hostname.isEmpty() -> {
                binding.editHostName.requestFocus()
                binding.editHostName.error = "Enter Host Name"
                return false
            }
            updateEvent.contactInfo.isEmpty() -> {
                binding.editContactInfoOne.requestFocus()
                binding.editContactInfoOne.error = "Enter Contact Info"
                return false
            }
            updateEvent.startTime.isEmpty() -> {
                longSnackBar("Please select event start time", binding.constraintEditEvent)
                return false
            }
            updateEvent.endTime.isEmpty() -> {
                longSnackBar("Please select event end time", binding.constraintEditEvent)
                return false
            }
            updateEvent.startDate.isEmpty() -> {
                longSnackBar("Please select event start date", binding.constraintEditEvent)
                return false
            }
            updateEvent.endDate.isEmpty() -> {
                longSnackBar("Please select event end date", binding.constraintEditEvent)
                return false
            }

            else -> return true
        }

    }

    private fun setEditEventData() {

        if (myEvents != null){

            binding.editEventName.setText(myEvents.event_name)
            binding.editHostName.setText(myEvents.hostname)
            binding.editContactInfoOne.setText(myEvents.contactinfo)
            binding.editCountry.setText(myEvents.country)
            binding.editState.setText(myEvents.state)
            binding.editCity.setText(myEvents.city)
            binding.editZipCode.setText(myEvents.zipcode)
            binding.editStreetAddress.setText(myEvents.streetaddress)
            binding.editStartDate.setText(myEvents.startdate)
            binding.editEndDate.setText(myEvents.enddate)
            binding.editStartTime.setText(myEvents.starttime)
            binding.editEndTime.setText(myEvents.endtime)
            binding.editEventDescription.setText(myEvents.event_description)

            /*if (myEvents.photo.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(myEvents.photo)
                    .into(binding.editEventImage)
            }*/
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
                //binding.editEventImage.setImageBitmap(result.data?.extras!!.get("data") as Bitmap)
                selectedImageUri = getImageUri(requireContext(),
                    result.data?.extras!!.get("data") as Bitmap
                )
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
            //binding.editEventImage.setImageURI(selectedImageUri)
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

            updateEvent.startDate = selectedDate
            binding.editStartDate.setText(datePicker.headerText)

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

            updateEvent.endDate = selectedDate
            binding.editEndDate.setText(datePicker.headerText)

        }

        datePicker.addOnNegativeButtonClickListener {
            datePicker.dismiss()
        }

        datePicker.show(requireActivity().supportFragmentManager,
            context?.resources?.getString(R.string.date_picker)
        )

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
                updateEvent.startTime = selectedTime
                binding.editStartTime.setText(selectedTime)
            }else{
                updateEvent.endTime = selectedTime
                binding.editEndTime.setText(selectedTime)
            }
        }
        timePicker.addOnNegativeButtonClickListener {
            timePicker.dismiss()
        }
        timePicker.show(requireActivity().supportFragmentManager,
            context?.resources?.getString(R.string.time_picker))
    }

    @SuppressLint("SetTextI18n")
    private fun showBottomSheet(){
        bottomSheet = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(requireContext().applicationContext)
            .inflate(
                R.layout.authentication_progress_bottom_sheet,
                requireActivity().findViewById<ConstraintLayout>(R.id.bottom_sheet)
            )
        val title = view.findViewById<TextView>(R.id.progress_title)
        val subTitle = view.findViewById<TextView>(R.id.progress_sub_title)

        title.text = "Updating Event"
        subTitle.text = "Updating edited event, please wait..."

        bottomSheet.setCancelable(false)
        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    private  fun hideBottomSheet(){
        bottomSheet.hide()
    }

    private fun showSuccessAlert(title: String, message: String) {
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Ok") { dialogInterface, i ->
                dialogInterface.dismiss()
                findNavController().popBackStack()
            }
            .create()
        if (dialog.window != null)
            dialog.window?.attributes?.windowAnimations = R.style.SlidingDialogAnimation

        dialog.show()
    }

}