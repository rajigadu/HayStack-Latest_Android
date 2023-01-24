package com.haystackevents.app.`in`.view.viewpager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.haystackevents.app.`in`.view.viewpager.*
import com.haystackevents.app.`in`.databinding.FragmentEventsBinding
import com.haystackevents.app.`in`.utils.AppConstants.ARG_OBJECTS
import com.haystackevents.app.`in`.view.activity.MainMenuActivity

class EventsViewpagerFragment: Fragment() {

    private var binding: FragmentEventsBinding? = null
    private var tabTitles = arrayOf("My Events", "Interests", "Attend", "Invited")
    private lateinit var viewPagerAdapter: EventsViewPagerAdapter

    private var currentPosition: Int? = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEventsBinding.inflate(layoutInflater)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentPosition = arguments?.getInt(ARG_OBJECTS)
        activity?.supportFragmentManager?.setFragmentResultListener(
            "fragment-edit-events-callback", viewLifecycleOwner
        ) { key, bundle ->
            if (key == "fragment-edit-events-callback") {
                currentPosition = bundle.getInt(ARG_OBJECTS)
            }
        }
        //Log.e("TAG", "currentPosition: $currentPosition")

        viewPagerAdapter = EventsViewPagerAdapter(requireActivity())
        viewPagerAdapter.addFragment(MyEventsFragment())
        viewPagerAdapter.addFragment(InterestsEventsFragment())
        viewPagerAdapter.addFragment(AttendEventsFragment())
        viewPagerAdapter.addFragment(InvitedEventsFragment())

        binding?.myBookingViewPager?.adapter = viewPagerAdapter
        binding?.myBookingViewPager?.post {
            binding?.myBookingViewPager?.setCurrentItem(currentPosition!!, true)
        }

        binding?.myEventsTabs?.let {
            binding?.myBookingViewPager?.let { it1 ->
                TabLayoutMediator(it, it1) { tab, position ->
                    tab.text = tabTitles[position]
                    binding?.myBookingViewPager?.setCurrentItem(tab.position, true)
                }.attach()
            }
        }

        binding?.toolbarMyEvents?.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainMenuActivity).hideBottomNav()
    }
}