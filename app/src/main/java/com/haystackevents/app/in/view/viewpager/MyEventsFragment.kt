package com.haystackevents.app.`in`.view.viewpager

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.databinding.FragmentMyEventsBinding
import com.haystackevents.app.`in`.manager.SessionManager
import com.haystackevents.app.`in`.network.repository.Repository
import com.haystackevents.app.`in`.network.response.group_members.DefaultResponse
import com.haystackevents.app.`in`.network.response.my_events.MyEvents
import com.haystackevents.app.`in`.network.response.my_events.MyEventsData
import com.haystackevents.app.`in`.utils.AppConstants.ARG_OBJECTS
import com.haystackevents.app.`in`.utils.AppConstants.ARG_SERIALIZABLE
import com.haystackevents.app.`in`.utils.Extensions.getCurrentDate
import com.haystackevents.app.`in`.utils.Extensions.longSnackBar
import com.haystackevents.app.`in`.utils.Extensions.showAlertDialog
import com.haystackevents.app.`in`.utils.Extensions.showErrorResponse
import com.haystackevents.app.`in`.utils.RecyclerViewCustomAnimation
import com.haystackevents.app.`in`.view.viewpager.adapter.MyEventsRecyclerViewAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyEventsFragment: Fragment(), MyEventsRecyclerViewAdapter.MyEventsOnClickListener {

    private lateinit var binding: FragmentMyEventsBinding
    private lateinit var bottomSheet: BottomSheetDialog
    private lateinit var myEventsAdapter: MyEventsRecyclerViewAdapter
    private var listMyEvents = arrayListOf<MyEventsData>()
    private var endTime: String? = ""

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

        binding.refreshMyEvents.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.colorPrimary))

        binding.refreshMyEvents.setOnRefreshListener {
            listMyEvents.clear()
            getMyEvents()
        }

        myEventsAdapter = MyEventsRecyclerViewAdapter(requireContext())
        binding.recyclerMyEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myEventsAdapter
            itemAnimator = RecyclerViewCustomAnimation()
        }

    }

    private fun getMyEvents() {
        binding.refreshMyEvents.isRefreshing = true
        val currentDate = getCurrentDate()
        Repository.getMyEvents(currentDate, endTime = endTime!!).enqueue(object :
        Callback<MyEvents>{
            override fun onResponse(call: Call<MyEvents>, response: Response<MyEvents>) {
                try {

                    if (response.isSuccessful){
                        if (response.body()?.status == "1"){
                            binding.noEventsImage.visibility = View.INVISIBLE
                            binding.noEventsText.visibility = View.INVISIBLE

                            if (response.body()?.data != null){
                                listMyEvents.clear()
                                listMyEvents.addAll(response.body()?.data!!)
                                myEventsAdapter.update(listMyEvents, this@MyEventsFragment)
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

            override fun onFailure(call: Call<MyEvents>, t: Throwable) {
                showErrorResponse(t, binding.constraintMyEvents)
                binding.refreshMyEvents.isRefreshing = false
            }

        })
    }

    override fun onResume() {
        super.onResume()
        getMyEvents()
    }

    override fun myEventsItemCLick(events: MyEventsData) {
        val bundle = bundleOf(
            ARG_OBJECTS to "My Events",
            ARG_SERIALIZABLE to events
        )
        findNavController().navigate(R.id.eventsInfoFragment, bundle)
    }

    override fun deleteMyEvent(events: MyEventsData) {
        showBottomSheet()
        Repository.deleteMyEvents(events.id, SessionManager.instance.getUserId()).enqueue(
            object : Callback<DefaultResponse>{
                override fun onResponse(
                    call: Call<DefaultResponse>,
                    response: Response<DefaultResponse>
                ) {
                    try {

                        if (response.isSuccessful){
                            if (response.body()?.status == "1"){

                                showAlertDialog("Event Deleted", requireContext(),
                                    response.body()?.message)
                                getMyEvents()

                            }else{
                                showAlertDialog("Error Occurred",
                                requireContext(),
                                response.body()?.message)
                            }
                        }

                    }catch (e: Exception){e.printStackTrace()}
                    hideBottomSheet()
                }

                override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                    try {
                        showErrorResponse(t,binding.constraintMyEvents)
                    }catch (e: Exception){e.printStackTrace()}
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

    override fun editMyEvent(events: MyEventsData) {
        val bundle = bundleOf(ARG_SERIALIZABLE to events)
        findNavController().navigate(R.id.action_myEvents_to_editEvents, bundle)
    }
}