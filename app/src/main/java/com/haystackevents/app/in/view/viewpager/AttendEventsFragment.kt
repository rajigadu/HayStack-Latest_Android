package com.haystackevents.app.`in`.view.viewpager

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
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
import com.haystackevents.app.`in`.network.response.attend_events.AttendEvents
import com.haystackevents.app.`in`.network.response.attend_events.AttendEventsData
import com.haystackevents.app.`in`.network.response.group_members.DefaultResponse
import com.haystackevents.app.`in`.utils.AppConstants.EVENT_TYPE_ATTEND
import com.haystackevents.app.`in`.utils.Extensions
import com.haystackevents.app.`in`.utils.Extensions.getCurrentDate
import com.haystackevents.app.`in`.utils.Extensions.longSnackBar
import com.haystackevents.app.`in`.utils.Extensions.showAlertDialog
import com.haystackevents.app.`in`.utils.Extensions.showSnackBar
import com.haystackevents.app.`in`.view.viewpager.adapter.AttendEventsAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AttendEventsFragment: Fragment(), AttendEventsAdapter.AttendEventsItemClick {

    private lateinit var binding: FragmentMyEventsBinding
    private lateinit var attendEventsAdapter: AttendEventsAdapter
    private lateinit var bottomSheet: BottomSheetDialog
    private var currentDate: String? = null
    private var endTime: String? = ""
    private var listAttendEvents = arrayListOf<AttendEventsData>()


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

        binding.refreshMyEvents.setColorSchemeColors(
            ContextCompat.getColor(requireContext(),
            R.color.colorPrimary))

        binding.refreshMyEvents.setOnRefreshListener {
            listAttendEvents.clear()
            attendedEvents()
        }

        attendEventsAdapter = AttendEventsAdapter(requireContext())
        binding.recyclerMyEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = attendEventsAdapter
        }
    }

    private fun attendedEvents() {
        currentDate = getCurrentDate()
        binding.refreshMyEvents.isRefreshing = true
        Repository.getAttendEvents(currentDate!!, endTime!!).enqueue(
            object : Callback<AttendEvents> {
                override fun onResponse(
                    call: Call<AttendEvents>,
                    response: Response<AttendEvents>
                ) {
                    Log.e("TAG", "response: $response")
                    try {
                        if (response.isSuccessful){
                            if (response.body()?.status == "1"){

                                binding.noEventsImage.visibility = INVISIBLE
                                binding.noEventsText.visibility = INVISIBLE

                                if (response.body()?.data != null){
                                    listAttendEvents.clear()
                                    listAttendEvents.addAll(listOf(response.body()?.data!!))
                                }
                                attendEventsAdapter.update(listAttendEvents, this@AttendEventsFragment)

                            }else{
                                binding.noEventsImage.visibility = VISIBLE
                                binding.noEventsText.visibility = VISIBLE
                                longSnackBar(response.body()?.message!!, binding.constraintMyEvents)
                            }
                        }

                    }catch (e: Exception){e.printStackTrace()}
                    binding.refreshMyEvents.isRefreshing = false
                }

                override fun onFailure(call: Call<AttendEvents>, t: Throwable) {
                    Extensions.showErrorResponse(t, binding.constraintMyEvents)
                    binding.refreshMyEvents.isRefreshing = false
                }

            })
    }

    override fun onResume() {
        super.onResume()
        attendedEvents()
    }

    override fun deleteAttendEvent(attendEvent: AttendEventsData, position: Int) {
        showBottomSheet()
        Repository.deleteOtherEvents(attendEvent.id, SessionManager.instance.getUserId(), EVENT_TYPE_ATTEND)
            .enqueue(object : Callback<DefaultResponse>{
                override fun onResponse(
                    call: Call<DefaultResponse>,
                    response: Response<DefaultResponse>
                ) {
                    try {

                        if (response.isSuccessful){
                            if (response.body()?.status == "1"){
                                showAlertDialog(
                                    "Event Deleted",
                                    requireContext(),
                                    response.body()?.message)
                                listAttendEvents.removeAt(position)
                                attendedEvents()
                            }else{
                                showAlertDialog(
                                    "Error Occurred",
                                    requireContext(),
                                    response.body()?.message)
                                attendedEvents()
                            }
                        }

                    }catch (e: Exception){e.printStackTrace()}
                    hideBottomSheet()
                }

                override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                    showSnackBar(binding.constraintMyEvents, "something went wrong")
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

        bottomSheet.setCancelable(false)
        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    private  fun hideBottomSheet(){
        bottomSheet.hide()
    }
}