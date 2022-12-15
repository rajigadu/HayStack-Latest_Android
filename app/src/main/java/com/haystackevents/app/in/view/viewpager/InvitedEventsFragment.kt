package com.haystackevents.app.`in`.view.viewpager

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
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
import com.haystackevents.app.`in`.utils.Extensions.longSnackBar
import com.haystackevents.app.`in`.view.viewpager.adapter.InvitedEventsAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InvitedEventsFragment: Fragment(), InvitedEventsAdapter.InvitedEventsItemClick {

    private lateinit var binding: FragmentMyEventsBinding
    private var invitedEventsAdapter: InvitedEventsAdapter? = null
    private var currentDate: String? = null
    private var endTime: String? = ""
    private var listInvitedEvents = arrayListOf<InvitedEventsData>()
    private var bottomSheet: BottomSheetDialog? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyEventsBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.refreshMyEvents.setColorSchemeColors(ContextCompat.getColor(requireContext(),
            R.color.colorPrimary))

        binding.refreshMyEvents.setOnRefreshListener {
            listInvitedEvents.clear()
            invitedEvents()
        }

        invitedEventsAdapter = InvitedEventsAdapter(requireContext())
        binding.recyclerMyEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = invitedEventsAdapter
        }


    }

    private fun invitedEvents() {
        binding.refreshMyEvents.isRefreshing = true
        currentDate = getCurrentDate()
        Repository.getInvitedEvents(currentDate!!, endTime!!).enqueue(
            object : Callback<InvitedEvents> {
                override fun onResponse(
                    call: Call<InvitedEvents>,
                    response: Response<InvitedEvents>
                ) {

                    try {

                        if (response.isSuccessful){
                            if (response.body()?.status == "1"){
                                binding.noEventsImage.visibility = View.INVISIBLE
                                binding.noEventsText.visibility = View.INVISIBLE

                                response.body()?.data?.let { data ->
                                    listInvitedEvents.clear()
                                    listInvitedEvents.addAll(listOf(data))
                                    invitedEventsAdapter?.update(listInvitedEvents, this@InvitedEventsFragment)
                                }

                            }else{
                                binding.noEventsImage.visibility = View.VISIBLE
                                binding.noEventsText.visibility = View.VISIBLE
                                longSnackBar(response.body()?.message!!, binding.constraintMyEvents)
                            }
                        }

                    }catch (e: Exception){e.printStackTrace()}
                    binding.refreshMyEvents.isRefreshing = false
                }

                override fun onFailure(call: Call<InvitedEvents>, t: Throwable) {
                    Extensions.showErrorResponse(t, binding.constraintMyEvents)
                    binding.refreshMyEvents.isRefreshing = false
                }

            })
    }

    override fun onResume() {
        super.onResume()
        invitedEvents()
    }

    override fun deleteInvitedEvent(invitedEvents: InvitedEventsData) {
        showBottomSheet()
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
                    hideBottomSheet()
                }

                override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                    Extensions.showSnackBar(binding.constraintMyEvents, "something went wrong")
                    hideBottomSheet()
                }

            })
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
        val subtitle = view.findViewById<TextView>(R.id.progress_sub_title)

        title.text = "Deleting Event"
        subtitle.text = "Deleting event, please wait...."

        bottomSheet?.setCancelable(false)
        bottomSheet?.setContentView(view)
        bottomSheet?.show()
    }

    private  fun hideBottomSheet(){
        bottomSheet?.hide()
    }
}