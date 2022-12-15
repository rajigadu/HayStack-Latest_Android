package com.haystackevents.app.`in`.view.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.haystackevents.app.`in`.databinding.LayoutNearEventsListItemBinding
import com.haystackevents.app.`in`.network.response.near_events.NearEventsData
import com.haystackevents.app.`in`.view.fragments.MapFragment
import java.util.*

class NearEventsAdapter(var context: Context)
    : RecyclerView.Adapter<NearEventsAdapter.ViewHolder>() {

    private var listNearEvents = arrayListOf<NearEventsData>()
    private var clickListener: NearEventsOnClick? = null



    inner class ViewHolder(val binding: LayoutNearEventsListItemBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bindView(nearEvents: NearEventsData) {
            binding.eventName.text = nearEvents.event_name
            binding.hostName.text = nearEvents.hostname
            binding.hostNumber.text = nearEvents.contactinfo

            binding.eventItem.setOnClickListener {
                clickListener?.nearEventClick(nearEvents)
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

    fun update(listNearestEvents: ArrayList<NearEventsData>, listener: MapFragment){
        this.listNearEvents = listNearestEvents
        this.clickListener = listener
        notifyDataSetChanged()
    }

    interface NearEventsOnClick{
        fun nearEventClick(nearEvents: NearEventsData)
    }
}