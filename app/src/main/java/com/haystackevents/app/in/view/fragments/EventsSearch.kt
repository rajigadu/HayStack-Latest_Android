package com.haystackevents.app.`in`.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.databinding.FragmentEventSearchBinding
import com.haystackevents.app.`in`.manager.SessionManager
import com.haystackevents.app.`in`.network.repository.Repository
import com.haystackevents.app.`in`.network.response.search_events.SearchByEvent
import com.haystackevents.app.`in`.network.response.search_events.SearchEvents
import com.haystackevents.app.`in`.network.response.search_events.SearchEventsData
import com.haystackevents.app.`in`.utils.AppConstants.ARG_OBJECTS
import com.haystackevents.app.`in`.utils.AppConstants.ARG_SERIALIZABLE
import com.haystackevents.app.`in`.utils.AppConstants.EventTypes.SEARCH_EVENT
import com.haystackevents.app.`in`.utils.Extensions.longSnackBar
import com.haystackevents.app.`in`.utils.Extensions.showErrorResponse
import com.haystackevents.app.`in`.view.activity.MainMenuActivity
import com.haystackevents.app.`in`.view.adapters.EventSearchListAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EventsSearch: Fragment(), EventSearchListAdapter.EventSearchListItemClick {

    private var binding: FragmentEventSearchBinding? = null
    private lateinit var eventSearchListAdapter: EventSearchListAdapter
    private var searchEvent: SearchByEvent? = null

    private var listSearchedEvents = arrayListOf<SearchEventsData>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEventSearchBinding.inflate(layoutInflater)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.refreshSearchEvents?.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.colorPrimary))

        searchEvent = arguments?.getSerializable(ARG_SERIALIZABLE) as? SearchByEvent

        binding?.toolbarEventsSearch?.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding?.toolbarEventsSearch?.setOnMenuItemClickListener {
            when(it?.itemId) {
                R.id.actionHome -> {
                    findNavController().navigate(R.id.action_eventsSearch_to_homeFragment)
                }
            }
            return@setOnMenuItemClickListener false
        }

        eventSearchListAdapter = EventSearchListAdapter(requireContext(), this)
        binding?.eventsSearchListView?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventSearchListAdapter
        }

        binding?.refreshSearchEvents?.setOnRefreshListener {
            listSearchedEvents.clear()
            getSearchEvents()
        }

    }

    private fun getSearchEvents() {
        searchEvent?.id = SessionManager.instance.getUserId()
        Log.e("TAG", "searchEvent: $searchEvent")
        binding?.refreshSearchEvents?.isRefreshing = true
        Repository.searchEvent(searchEvent).enqueue(object : Callback<SearchEvents>{
            override fun onResponse(call: Call<SearchEvents>, response: Response<SearchEvents>) {

                try {

                    if (response.isSuccessful){
                        if (response.body()?.status == "1"){

                            if (response.body()?.data != null){
                                binding?.noEventsImage?.isVisible = false
                                binding?.noEventsText?.isVisible = false
                                listSearchedEvents.clear()
                                listSearchedEvents.addAll(response.body()?.data!!)
                                eventSearchListAdapter.update(listSearchedEvents)
                            } else {
                                binding?.noEventsImage?.isVisible = true
                                binding?.noEventsText?.isVisible = true
                            }

                        }else{
                            longSnackBar(response.body()?.message!!, binding?.constraintEventSearch)
                        }
                    }

                }catch (e: Exception){e.printStackTrace()}
                binding?.refreshSearchEvents?.isRefreshing = false
            }

            override fun onFailure(call: Call<SearchEvents>, t: Throwable) {
                showErrorResponse(t, binding?.constraintEventSearch)
                binding?.refreshSearchEvents?.isRefreshing = false
            }

        })
    }

    override fun eventListItemClick(data: SearchEventsData) {
        val bundle = bundleOf(
            ARG_OBJECTS to SEARCH_EVENT,
            ARG_SERIALIZABLE to data
        )
        findNavController().navigate(R.id.action_eventsSearch_to_eventsInfoFragment, bundle)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainMenuActivity).hideBottomNav()
        getSearchEvents()
    }
}