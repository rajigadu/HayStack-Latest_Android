package com.haystackevents.app.`in`.view.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.databinding.FragmentEventInfoBinding
import com.haystackevents.app.`in`.manager.SessionManager
import com.haystackevents.app.`in`.network.config.AppConfig.IMAGE_BASE_URL
import com.haystackevents.app.`in`.network.repository.Repository
import com.haystackevents.app.`in`.network.response.add_attend_events.AddAttendEvent
import com.haystackevents.app.`in`.network.response.add_interest_events.AddInterestEvents
import com.haystackevents.app.`in`.network.response.my_events.MyEventsData
import com.haystackevents.app.`in`.network.response.near_events.NearEventsData
import com.haystackevents.app.`in`.network.response.nearest_events.NearestEventData
import com.haystackevents.app.`in`.network.response.search_events.SearchEventsData
import com.haystackevents.app.`in`.utils.AppConstants
import com.haystackevents.app.`in`.utils.AppConstants.ARG_OBJECTS
import com.haystackevents.app.`in`.utils.AppConstants.ARG_SERIALIZABLE
import com.haystackevents.app.`in`.utils.Extensions.showAlertDialog
import com.haystackevents.app.`in`.utils.Extensions.showErrorResponse
import com.haystackevents.app.`in`.utils.ProgressCaller
import com.haystackevents.app.`in`.view.activity.MainMenuActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EventsInfoFragment: Fragment() {

    private var binding: FragmentEventInfoBinding? = null
    private lateinit var eventInfo: SearchEventsData
    private lateinit var myEvents: MyEventsData
    private lateinit var nearestEvents: NearestEventData
    private lateinit var nearEvents: NearEventsData

    private var latitude: String? = null
    private var longitude: String? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEventInfoBinding.inflate(layoutInflater)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (arguments?.getString(ARG_OBJECTS)) {
            AppConstants.EventTypes.SEARCH_EVENT -> {
                eventInfo = arguments?.getSerializable(ARG_SERIALIZABLE) as SearchEventsData
                setEventInfoData(eventInfo)
            }
            AppConstants.EventTypes.MY_EVENT -> {
                myEvents = arguments?.getSerializable(ARG_SERIALIZABLE) as MyEventsData
                setMyEventsData(myEvents)
            }
            AppConstants.EventTypes.NEAREST_EVENT -> {
                nearestEvents = arguments?.getSerializable(ARG_SERIALIZABLE) as NearestEventData
                setNearestEventsData(nearestEvents)
            }
            AppConstants.EventTypes.NEAR_EVENT -> {
                nearEvents = arguments?.getSerializable(ARG_SERIALIZABLE) as NearEventsData
                setNearEventsData(nearEvents)
            }
        }

        binding?.toolbarEventInfo?.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding?.btnNotInterested?.setOnClickListener {
            findNavController().navigate(R.id.action_eventsInfoFragment_to_homeFragment)
        }

        binding?.btnInterested?.setOnClickListener {
            addEventsToInterested()
        }

        binding?.btnAttend?.setOnClickListener {
            addEventsToAttend()
        }

        binding?.toolbarEventInfo?.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.actionMap -> {
                    //val gmmIntentUri = Uri.parse("geo:$latitude,$longitude")
                    val gmmIntentUri = Uri.parse(
                        "http://maps.google.com/maps?saddr=${SessionManager.instance.getUserLatLng().latitude}," +
                                "${SessionManager.instance.getUserLatLng().longitude}&daddr=$latitude,$longitude")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    mapIntent.resolveActivity(requireActivity().packageManager)?.let {
                        startActivity(mapIntent)
                    }
                    //Log.e("TAG", "gmmIntentUri: $gmmIntentUri")

                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
    }

    private fun setNearEventsData(nearEvents: NearEventsData) {
        binding?.btnAttend?.visibility = GONE
        binding?.btnInterested?.visibility = GONE
        binding?.btnNotInterested?.visibility = GONE

        binding?.textEventName?.text = nearEvents.event_name
        binding?.textHostName?.text = nearEvents.hostname
        binding?.textContactInfo?.text = nearEvents.contactinfo
        binding?.textCountry?.text = nearEvents.country
        binding?.textState?.text = nearEvents.state
        binding?.textCity?.text = nearEvents.city
        binding?.textZipCode?.text = nearEvents.zipcode
        binding?.textStreetAddress?.text = nearEvents.streetaddress
        binding?.textStartDate?.text = nearEvents.startdate
        binding?.textStartTime?.text = nearEvents.starttime
        binding?.textEndDate?.text = nearEvents.enddate
        binding?.textEndTime?.text = nearEvents.endtime
        binding?.textEventDesciption?.setText(nearEvents.event_description)

        latitude = nearEvents.latitude
        longitude = nearEvents.longitude

        nearEvents.photo?.let { photo ->
            binding?.eventImage?.let {
                Glide.with(requireContext())
                    .load(IMAGE_BASE_URL + photo)
                    .placeholder(R.drawable.events_default_bg_)
                    .into(it)
            }
        }
        binding?.imgProgress?.isVisible = false
    }

    private fun setNearestEventsData(nearestEvents: NearestEventData) {
        binding?.btnAttend?.visibility = GONE
        binding?.btnInterested?.visibility = GONE
        binding?.btnNotInterested?.visibility = GONE

        binding?.textEventName?.text = nearestEvents.event_name
        binding?.textHostName?.text = nearestEvents.hostname
        binding?.textContactInfo?.text = nearestEvents.contactinfo
        binding?.textCountry?.text = nearestEvents.country
        binding?.textState?.text = nearestEvents.state
        binding?.textCity?.text = nearestEvents.city
        binding?.textZipCode?.text = nearestEvents.zipcode
        binding?.textStreetAddress?.text = nearestEvents.streetaddress
        binding?.textStartDate?.text = nearestEvents.startdate
        binding?.textStartTime?.text = nearestEvents.starttime
        binding?.textEndDate?.text = nearestEvents.enddate
        binding?.textEndTime?.text = nearestEvents.endtime
        binding?.textEventDesciption?.setText(nearestEvents.event_description)

        latitude = nearestEvents.latitude
        longitude = nearestEvents.longitude

        nearestEvents.photo?.let { photo ->
            binding?.eventImage?.let {
                Glide.with(requireContext())
                    .load(IMAGE_BASE_URL + photo)
                    .placeholder(R.drawable.events_default_bg_)
                    .into(it)
            }
        }
        binding?.imgProgress?.isVisible = false
    }

    private fun setMyEventsData(myEvents: MyEventsData) {
        binding?.btnAttend?.visibility = GONE
        binding?.btnInterested?.visibility = GONE
        binding?.btnNotInterested?.visibility = GONE

        binding?.textEventName?.text = myEvents.event_name
        binding?.textHostName?.text = myEvents.hostname
        binding?.textContactInfo?.text = myEvents.contactinfo
        binding?.textCountry?.text = myEvents.country
        binding?.textState?.text = myEvents.state
        binding?.textCity?.text = myEvents.city
        binding?.textZipCode?.text = myEvents.zipcode
        binding?.textStreetAddress?.text = myEvents.streetaddress
        binding?.textStartDate?.text = myEvents.startdate
        binding?.textStartTime?.text = myEvents.starttime
        binding?.textEndDate?.text = myEvents.enddate
        binding?.textEndTime?.text = myEvents.endtime
        binding?.textEventDesciption?.setText(myEvents.event_description)

        latitude = myEvents.latitude
        longitude = myEvents.longitude

        myEvents.photo?.let { photo ->
            binding?.eventImage?.let {
                Glide.with(requireContext())
                    .load(IMAGE_BASE_URL + photo)
                    .placeholder(R.drawable.events_default_bg_)
                    .into(it)
            }
        }
        binding?.imgProgress?.isVisible = false
    }

    private fun addEventsToAttend() {
        context?.let { ProgressCaller.showProgressDialog(it) }
        Repository.eventAddToAttend(eventInfo.id, eventInfo.userid).enqueue(object :Callback<AddAttendEvent>{
            override fun onResponse(
                call: Call<AddAttendEvent>,
                response: Response<AddAttendEvent>
            ) {
                try {

                    if (response.isSuccessful){
                        if (response.body()?.status == "1"){
                            showSuccessAlert("Attend","Event Added Successfully", response.body()?.message!!)
                        }else{
                            showAlertDialog(
                                "Error Occurred?",
                                requireContext(),
                                response.body()?.message
                            )
                        }
                    }

                }catch (e: Exception){e.printStackTrace()}
                ProgressCaller.hideProgressDialog()
            }

            override fun onFailure(call: Call<AddAttendEvent>, t: Throwable) {
                showErrorResponse(t, binding?.constraintContactInfo)
                ProgressCaller.hideProgressDialog()
            }

        })
    }

    private fun addEventsToInterested() {
        context?.let { ProgressCaller.showProgressDialog(it) }
        Repository.eventAddToInterested(eventInfo.id, eventInfo.userid).enqueue(object
            :Callback<AddInterestEvents>{
            override fun onResponse(
                call: Call<AddInterestEvents>,
                response: Response<AddInterestEvents>
            ) {
                try {

                    if (response.isSuccessful){
                        if (response.body()?.status == "1"){

                            showSuccessAlert("Interested", "Event Added Successfully", response.body()?.message!!)

                        }else{
                            showAlertDialog(
                                "Error Occurred?",
                                requireContext(),
                                response.body()?.message
                            )
                        }
                    }

                }catch (e: Exception){e.printStackTrace()}
                ProgressCaller.hideProgressDialog()
            }

            override fun onFailure(call: Call<AddInterestEvents>, t: Throwable) {
                showErrorResponse(t, binding?.constraintContactInfo)
                ProgressCaller.hideProgressDialog()
            }

        })
    }

    private fun setEventInfoData(eventInfo: SearchEventsData) {
        binding?.btnAttend?.visibility = VISIBLE
        binding?.btnInterested?.visibility = VISIBLE
        binding?.btnNotInterested?.visibility = VISIBLE

        binding?.textEventName?.text = eventInfo.event_name
        binding?.textHostName?.text = eventInfo.hostname
        binding?.textContactInfo?.text = eventInfo.contactinfo
        binding?.textCountry?.text = eventInfo.country
        binding?.textState?.text = eventInfo.state
        binding?.textCity?.text = eventInfo.city
        binding?.textZipCode?.text = eventInfo.zipcode
        binding?.textStreetAddress?.text = eventInfo.streetaddress
        binding?.textStartDate?.text = eventInfo.startdate
        binding?.textStartTime?.text = eventInfo.starttime
        binding?.textEndDate?.text = eventInfo.enddate
        binding?.textEndTime?.text = eventInfo.endtime
        binding?.textEventDesciption?.setText(eventInfo.event_description)

        latitude = eventInfo.latitude
        longitude = eventInfo.longitude

        eventInfo.photo?.let { photo ->
            binding?.eventImage?.let {
                Glide.with(requireContext())
                    .load(IMAGE_BASE_URL + photo)
                    .placeholder(R.drawable.events_default_bg_)
                    .into(it)
            }
        }
        binding?.imgProgress?.isVisible = false
    }

    private fun showSuccessAlert(s: String, title: String, message: String) {
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Ok") { dialogInterface, i ->
                dialogInterface.dismiss()
                if (s == "Attend") {
                    val bundle = bundleOf(ARG_OBJECTS to 2)
                    findNavController().navigate(R.id.action_eventsInfoFragment_to_myEvents, bundle)
                }
                else {
                    val bundle = bundleOf(ARG_OBJECTS to 1)
                    findNavController().navigate(R.id.action_eventsInfoFragment_to_myEvents, bundle)
                }
            }
            .create()
        if (dialog.window != null)
            dialog.window?.attributes?.windowAnimations = R.style.SlidingDialogAnimation

        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        (activity as MainMenuActivity).hideBottomNav()
    }
}