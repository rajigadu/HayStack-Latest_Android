package com.haystackevents.app.`in`.view.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.haystackevents.app.`in`.databinding.LayoutEventSearchListItemBinding
import com.haystackevents.app.`in`.network.response.search_events.SearchEventsData
import com.haystackevents.app.`in`.view.fragments.EventsSearch
import java.util.ArrayList


class EventSearchListAdapter(val context: Context, val fragment: EventsSearch)
    : RecyclerView.Adapter<EventSearchListAdapter.ViewHolder>() {

    private lateinit var eventListItemClick: EventSearchListItemClick
    private var listEvents = arrayListOf<SearchEventsData>()


    inner class ViewHolder(val binding: LayoutEventSearchListItemBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bindView(data: SearchEventsData) {

            binding.eventManag.text = data.event_name
            binding.hostName.text = data.hostname
            binding.hostNumber.text = data.contactinfo
            binding.membersCount.text = "People (${data.membercount})"

            binding.eventItem.setOnClickListener {
                eventListItemClick.eventListItemClick(data)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventSearchListAdapter.ViewHolder =
        ViewHolder(LayoutEventSearchListItemBinding.inflate(LayoutInflater.from(
            parent.context
        ), parent, false))

    override fun onBindViewHolder(holder: EventSearchListAdapter.ViewHolder, position: Int) {
        eventListItemClick = fragment
        holder.bindView(listEvents[position])
    }

    override fun getItemCount(): Int = listEvents.size

    fun update(listSearchedEvents: ArrayList<SearchEventsData>) {
        this.listEvents = listSearchedEvents
        notifyDataSetChanged()
    }

    interface EventSearchListItemClick{
        fun eventListItemClick(data: SearchEventsData)
    }

}