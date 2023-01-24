package com.haystackevents.app.`in`.view.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.databinding.FragmentDateRangeBinding
import com.haystackevents.app.`in`.network.response.search_events.SearchByEvent
import com.haystackevents.app.`in`.utils.AppConstants.ARG_SERIALIZABLE
import com.haystackevents.app.`in`.utils.Extensions.getCurrentDate
import com.haystackevents.app.`in`.utils.Extensions.getCurrentTime
import com.haystackevents.app.`in`.utils.Extensions.longSnackBar
import com.haystackevents.app.`in`.view.activity.MainMenuActivity
import java.text.SimpleDateFormat
import java.util.*

class DateRangeFragment: Fragment() {


    private var binding: FragmentDateRangeBinding? = null
    private var searchEvent: SearchByEvent? = null
    private var lastClickTime: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDateRangeBinding.inflate(layoutInflater)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchEvent = arguments?.getSerializable(ARG_SERIALIZABLE) as SearchByEvent
        Log.e("TAG", "searchEvent: $searchEvent")

        clickListeners()

        setInitialValuesInEdittext()

    }

    private fun setInitialValuesInEdittext() {
        if (searchEvent?.startDate.isNullOrEmpty()) {
            binding?.inputStartDate?.setText(getCurrentDate())
            searchEvent?.startDate = binding?.inputStartDate?.text.toString().trim()
        } else{
            binding?.inputStartDate?.setText(searchEvent?.startDate)
        }
        if (searchEvent?.endDate.isNullOrEmpty()) {
            binding?.inputEndDate?.setText(getCurrentDate())
            searchEvent?.endDate = binding?.inputEndDate?.text.toString().trim()
        }else {
            binding?.inputEndDate?.setText(searchEvent?.endDate)
        }
        if (searchEvent?.startTime.isNullOrEmpty()) {
            binding?.inputStartTime?.setText(getCurrentTime())
        } else binding?.inputStartTime?.setText(searchEvent?.startTime)
        if (searchEvent?.endTime.isNullOrEmpty()) {
            binding?.inputEndTime?.setText(getCurrentTime())
        } else binding?.inputEndTime?.setText(searchEvent?.endTime)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun clickListeners() {

        binding?.toolbarDateRange?.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding?.btnContinue?.setOnClickListener {
            searchEvent?.startTime = binding?.inputStartTime?.text.toString().trim()
            searchEvent?.endTime = binding?.inputEndTime?.text.toString().trim()

            if (validated()){
                val bundle = bundleOf(ARG_SERIALIZABLE to searchEvent)
                findNavController().navigate(R.id.action_dateRangeFragment_to_eventsSearch, bundle)
            }

        }

        binding?.inputStartTime?.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action){
                MotionEvent.ACTION_UP -> {
                    if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                        return@setOnTouchListener false
                    }
                    lastClickTime = SystemClock.elapsedRealtime()
                    showTimePickerDialog("Select Event Start Time", "start")
                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }
        }

        binding?.inputEndTime?.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action){
                MotionEvent.ACTION_UP -> {
                    if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                        return@setOnTouchListener false
                    }
                    lastClickTime = SystemClock.elapsedRealtime()
                    showTimePickerDialog("Select Event End Time", "end")
                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }
        }

        binding?.inputStartDate?.setOnTouchListener { view, motionEvent ->
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

        binding?.inputEndDate?.setOnTouchListener { view, motionEvent ->
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
    }

    private fun validated(): Boolean {
        when{
            searchEvent?.startDate?.isEmpty() == true -> {
                longSnackBar("Please select start data", binding?.constraintDateRange)
                return false
            }
            searchEvent?.startTime?.isEmpty() == true -> {
                longSnackBar("Please select start time", binding?.constraintDateRange)
                return false
            }
            searchEvent?.endTime?.isEmpty() == true -> {
                longSnackBar("Please select end time", binding?.constraintDateRange)
                return false
            }
            searchEvent?.endDate?.isEmpty() == true-> {
                longSnackBar("Please select end data", binding?.constraintDateRange)
                return false
            }
            else -> return true
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainMenuActivity).hideBottomNav()
    }

    private fun showTimePickerDialog(timePickerTitle: String, timeKey: String) {
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

            if (timeKey == "start") {
                searchEvent?.startTime = selectedTime
                binding?.inputStartTime?.setText(selectedTime)
            } else if (timeKey == "end") {
                searchEvent?.endTime = selectedTime
                binding?.inputEndTime?.setText(selectedTime)
            }

        }
        timePicker.addOnNegativeButtonClickListener {
            timePicker.dismiss()
        }
        timePicker.show(requireActivity().supportFragmentManager,
            context?.resources?.getString(R.string.time_picker))
    }

    @SuppressLint("SimpleDateFormat")
    private fun showDatePickerDialog(datePickerTitle: String) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTheme(R.style.DatePickerTheme)
            .setCalendarConstraints(constraintsBuilder.build())
            .setTitleText(datePickerTitle)
            .build()

        datePicker.isCancelable = false
        datePicker.addOnPositiveButtonClickListener {

            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.time = Date(it)

            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            calendar.set(year,month, day)
            val format = SimpleDateFormat("MM-dd-yyyy")
            val strDate: String = format.format(calendar.time)

            searchEvent?.startDate = strDate
            binding?.inputStartDate?.setText(datePicker.headerText)
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

    @SuppressLint("SimpleDateFormat")
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

            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            calendar.set(year,month, day)
            val format = SimpleDateFormat("MM-dd-yyyy")
            val strDate: String = format.format(calendar.time)

            searchEvent?.endDate = strDate
            binding?.inputEndDate?.setText(datePicker.headerText)

        }

        datePicker.addOnNegativeButtonClickListener {
            datePicker.dismiss()
        }

        datePicker.show(requireActivity().supportFragmentManager,
            context?.resources?.getString(R.string.date_picker)
        )

    }

}