package com.haystackevents.app.`in`.view.viewpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.databinding.FragmentMyEventsBinding
import com.haystackevents.app.`in`.manager.SessionManager
import com.haystackevents.app.`in`.network.repository.Repository
import com.haystackevents.app.`in`.network.response.group_members.DefaultResponse
import com.haystackevents.app.`in`.network.response.invited_events.InvitedEvents
import com.haystackevents.app.`in`.network.response.invited_events.InvitedEventsData
import com.haystackevents.app.`in`.utils.AppConstants
import com.haystackevents.app.`in`.utils.Extensions
import com.haystackevents.app.`in`.utils.Extensions.getCurrentDate
import com.haystackevents.app.`in`.utils.Extensions.getCurrentTime
import com.haystackevents.app.`in`.utils.Extensions.longSnackBar
import com.haystackevents.app.`in`.utils.ProgressCaller
import com.haystackevents.app.`in`.view.viewpager.adapter.InvitedEventsAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InvitedEventsFragment: Fragment(), InvitedEventsAdapter.InvitedEventsItemClick {

    private var binding: FragmentMyEventsBinding? = null
    private var invitedEventsAdapter: InvitedEventsAdapter? = null
    private var currentDate: String? = null
    private var endTime: String? = ""
    private var listInvitedEvents = arrayListOf<InvitedEventsData>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyEventsBinding.inflate(layoutInflater)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.refreshMyEvents?.setColorSchemeColors(ContextCompat.getColor(requireContext(),
            R.color.colorPrimary))

        binding?.refreshMyEvents?.setOnRefreshListener {
            listInvitedEvents.clear()
            invitedEvents()
        }

        invitedEventsAdapter = InvitedEventsAdapter(requireContext())
        binding?.recyclerMyEvents?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = invitedEventsAdapter
        }


    }

    private fun invitedEvents() {
        binding?.refreshMyEvents?.isRefreshing = true
        currentDate = getCurrentDate()
        endTime = getCurrentTime()
        Repository.getInvitedEvents(currentDate!!, endTime!!).enqueue(
            object : Callback<InvitedEvents> {
                override fun onResponse(
                    call: Call<InvitedEvents>,
                    response: Response<InvitedEvents>
                ) {

                    try {

                        if (response.isSuccessful){
                            if (response.body()?.status == "1"){
                                binding?.noEventsImage?.visibility = View.INVISIBLE
                                binding?.noEventsText?.visibility = View.INVISIBLE

                                response.body()?.data?.let { data ->
                                    listInvitedEvents.clear()
                                    listInvitedEvents.addAll(data)
                                    invitedEventsAdapter?.update(listInvitedEvents, this@InvitedEventsFragment)
                                }

                            }else{
                                binding?.noEventsImage?.visibility = View.VISIBLE
                                binding?.noEventsText?.visibility = View.VISIBLE
                                longSnackBar(response.body()?.message!!, binding?.constraintMyEvents)
                            }
                        }

                    }catch (e: Exception){e.printStackTrace()}
                    binding?.refreshMyEvents?.isRefreshing = false
                }

                override fun onFailure(call: Call<InvitedEvents>, t: Throwable) {
                    Extensions.showErrorResponse(t, binding?.constraintMyEvents)
                    binding?.refreshMyEvents?.isRefreshing = false
                }

            })
    }

    override fun onResume() {
        super.onResume()
        if (isVisible) invitedEvents()
    }

    override fun deleteInvitedEvent(invitedEvents: InvitedEventsData) {
        context?.let { ProgressCaller.showProgressDialog(it) }
        Repository.deleteOtherEvents(invitedEvents.id, SessionManager.instance.getUserId(),
            AppConstants.EVENT_TYPE_INVITED)
            .enqueue(object : Callback<DefaultResponse>{
                override fun onResponse(
                    call: Call<DefaultResponse>,
                    response: Response<DefaultResponse>
                ) {
                    try {

                        if (response.isSuccessful){
                            if (response.body()?.status == "1"){
                                Extensions.showAlertDialog(
                                    "Delete Event",
                                    requireContext(),
                                    response.body()?.message
                                )
                                invitedEvents()
                            }else{
                                Extensions.showAlertDialog(
                                    "Delete Event",
                                    requireContext(),
                                    response.body()?.message
                                )
                            }
                        }

                    }catch (e: Exception){e.printStackTrace()}
                    ProgressCaller.hideProgressDialog()
                }

                override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                    Extensions.showSnackBar(binding?.constraintMyEvents, "something went wrong")
                    ProgressCaller.hideProgressDialog()
                }

            })
    }
}