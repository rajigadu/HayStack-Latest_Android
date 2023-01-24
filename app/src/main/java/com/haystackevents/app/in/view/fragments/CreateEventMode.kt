package com.haystackevents.app.`in`.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.databinding.FragmentCreateEventModeBinding
import com.haystackevents.app.`in`.network.response.event.Event
import com.haystackevents.app.`in`.utils.AppConstants.ARG_SERIALIZABLE
import com.haystackevents.app.`in`.view.activity.MainMenuActivity

class CreateEventMode: Fragment() {


    private var binding: FragmentCreateEventModeBinding? = null
    private var advertiseEvent: String = "Public"
    private var hostContactInfo: String = "Public"
    private var events: Event? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateEventModeBinding.inflate(layoutInflater)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        events = arguments?.getSerializable(ARG_SERIALIZABLE) as Event
        Log.e("TAG", "events: $events")

        binding?.toolbarEventMode?.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding?.btnContinue?.setOnClickListener {

            val checkedAdvertiseEventId = binding?.advertiseEventGroup?.checkedRadioButtonId
            findAdvertiseEvent(checkedAdvertiseEventId)
            val checkedHostContactInfoId = binding?.hostContactInfoGroup?.checkedRadioButtonId
            findHostContactInfo(checkedHostContactInfoId)

            if (advertiseEvent.isEmpty() || hostContactInfo.isEmpty()){
                return@setOnClickListener
            }
            events?.eventtype = advertiseEvent
            events?.hosttype = hostContactInfo
            val bundle = bundleOf(
                ARG_SERIALIZABLE to events,
                "flow" to "1"
            )
            findNavController().navigate(R.id.action_createEventMode_to_addMembersFragment, bundle)
        }

        binding?.toolbarEventMode?.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

    }

    private fun findAdvertiseEvent(checkedId: Int?) {
        when(checkedId){
            R.id.advertisePublic -> {
                advertiseEvent = "Public"
            }
            R.id.advertisePrivate -> {
                advertiseEvent = "Private"
            }
        }
    }

    private fun findHostContactInfo(checkedId: Int?) {
        when(checkedId){
            R.id.hostContactPublic -> {
                hostContactInfo = "Public"
            }
            R.id.hostContactPrivate -> {
                hostContactInfo = "Private"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainMenuActivity).hideBottomNav()
    }
}