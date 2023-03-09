package com.haystackevents.app.`in`.view.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.haystackevents.app.`in`.databinding.LayoutNearEventsListItemBinding
import com.haystackevents.app.`in`.network.response.near_events.NearEventsData
import com.haystackevents.app.`in`.utils.FragmentCallback
import com.haystackevents.app.`in`.view.fragments.MapFragment
import java.util.*

class NearEventsAdapter(var fragmentCallback: FragmentCallback?)
    : RecyclerView.Adapter<NearEventsAdapter.ViewHolder>() {

    private var listNearEvents = arrayListOf<NearEventsData>()

    inner class ViewHolder(val binding: LayoutNearEventsListItemBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bindView(nearEvents: NearEventsData) {
            binding.eventName.text = nearEvents.event_name
            binding.hostName.text = nearEvents.hostname
            binding.hostNumber.text = nearEvents.contactinfo

            binding.eventItem.setOnClickListener {
                fragmentCallback?.onResult(nearEvents)
            }
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            LayoutNearEventsListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(listNearEvents[position])
    }

    override fun getItemCount(): Int = listNearEvents.size

    @SuppressLint("NotifyDataSetChanged")
    fun update(listNearestEvents: ArrayList<NearEventsData>){
        this.listNearEvents = listNearestEvents
        notifyDataSetChanged()
    }
}