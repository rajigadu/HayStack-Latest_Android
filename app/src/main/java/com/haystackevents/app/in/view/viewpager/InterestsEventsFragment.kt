package com.haystackevents.app.`in`.view.viewpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.databinding.FragmentMyEventsBinding
import com.haystackevents.app.`in`.manager.SessionManager
import com.haystackevents.app.`in`.network.repository.Repository
import com.haystackevents.app.`in`.network.response.group_members.DefaultResponse
import com.haystackevents.app.`in`.network.response.interest_events.InterestEvents
import com.haystackevents.app.`in`.network.response.interest_events.InterestEventsData
import com.haystackevents.app.`in`.utils.AppConstants
import com.haystackevents.app.`in`.utils.Extensions
import com.haystackevents.app.`in`.utils.Extensions.getCurrentDate
import com.haystackevents.app.`in`.utils.Extensions.longSnackBar
import com.haystackevents.app.`in`.utils.Extensions.showAlertDialog
import com.haystackevents.app.`in`.utils.Extensions.showErrorResponse
import com.haystackevents.app.`in`.utils.ProgressCaller
import com.haystackevents.app.`in`.view.viewpager.adapter.InterestEventsAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InterestsEventsFragment: Fragment(), InterestEventsAdapter.InterestedEventsItemClick {

    private var binding: FragmentMyEventsBinding? = null
    private lateinit var interestEventsAdapter: InterestEventsAdapter
    private lateinit var bottomSheet: BottomSheetDialog
    private var currentDate: String? = null
    private var endTime: String? = ""
    private var listInterestEvents = arrayListOf<InterestEventsData>()


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

        interestEventsAdapter = InterestEventsAdapter(requireContext())
        binding?.recyclerMyEvents?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = interestEventsAdapter
        }

        binding?.refreshMyEvents?.setColorSchemeColors(ContextCompat.getColor(requireContext(),
            R.color.colorPrimary))

        binding?.refreshMyEvents?.setOnRefreshListener {
            listInterestEvents.clear()
            interestEvents()
        }

    }

    private fun interestEvents() {
        binding?.refreshMyEvents?.isRefreshing = true
        currentDate = getCurrentDate()
        endTime = Extensions.getCurrentTime()
        Repository.getInterestEvents(currentDate!!, endTime!!).enqueue(
            object : Callback<InterestEvents>{
                override fun onResponse(
                    call: Call<InterestEvents>,
                    response: Response<InterestEvents>
                ) {

                    try {

                        if (response.isSuccessful){
                            if (response.body()?.status == "1"){
                                binding?.noEventsImage?.visibility = View.INVISIBLE
                                binding?.noEventsText?.visibility = View.INVISIBLE

                                if (response.body()?.data != null) {
                                    listInterestEvents.clear()
                                    listInterestEvents.addAll(response.body()?.data!!)
                                    interestEventsAdapter.update(listInterestEvents, this@InterestsEventsFragment)
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

                override fun onFailure(call: Call<InterestEvents>, t: Throwable) {
                    showErrorResponse(t, binding?.constraintMyEvents)
                    binding?.refreshMyEvents?.isRefreshing = false
                }

            })
    }


    override fun onResume() {
        super.onResume()
        interestEvents()
    }

    override fun deleteInterestedEvent(interestEvent: InterestEventsData) {
        context?.let { ProgressCaller.showProgressDialog(it) }
        Repository.deleteOtherEvents(interestEvent.id, SessionManager.instance.getUserId(),
            AppConstants.EVENT_TYPE_INTEREST)
            .enqueue(object : Callback<DefaultResponse>{
                override fun onResponse(
                    call: Call<DefaultResponse>,
                    response: Response<DefaultResponse>
                ) {
                    try {

                        if (response.isSuccessful){
                            if (response.body()?.status == "1"){
                                showAlertDialog(
                                    "Delete Event",
                                    requireContext(),
                                    response.body()?.message
                                )
                                interestEvents()
                            }else{
                                showAlertDialog(
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