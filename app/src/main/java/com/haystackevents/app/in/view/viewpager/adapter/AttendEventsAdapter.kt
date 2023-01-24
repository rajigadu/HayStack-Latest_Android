package com.haystackevents.app.`in`.view.viewpager.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View.INVISIBLE
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.databinding.LayoutGroupsListItemViewBinding
import com.haystackevents.app.`in`.network.response.attend_events.AttendEventsData
import com.haystackevents.app.`in`.view.viewpager.AttendEventsFragment
import java.util.ArrayList

class AttendEventsAdapter(var context: Context)
    : RecyclerView.Adapter<AttendEventsAdapter.ViewHolder>() {

    private var listAttendEvents = arrayListOf<AttendEventsData>()
    private lateinit var eventOnClick: AttendEventsItemClick



    inner class ViewHolder(val binding: LayoutGroupsListItemViewBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bindView(attendEvent: AttendEventsData) {
            binding.editEventGroup.isVisible = false
            //binding.deleteGroup.visibility = INVISIBLE
            binding.eventName.text = attendEvent.event_name

            binding.deleteGroup.setOnClickListener {
                showConfirmationDialog(attendEvent, adapterPosition)
            }
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            LayoutGroupsListItemViewBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(listAttendEvents[position])
    }

    override fun getItemCount(): Int = listAttendEvents.size

    fun update(listAttendEvents: ArrayList<AttendEventsData>, onClick: AttendEventsFragment){
        this.listAttendEvents = listAttendEvents
        this.eventOnClick = onClick
        notifyDataSetChanged()
    }

    private fun showConfirmationDialog(attendEvent: AttendEventsData, position: Int) {
        val dialog = MaterialAlertDialogBuilder(context, R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle("Delete Event?")
            .setMessage("Are you sure want to delete this event.?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialogInterface, i ->
                dialogInterface.dismiss()
                eventOnClick.deleteAttendEvent(attendEvent, position)
            }
            .setNegativeButton("No") { dialogInterface, i ->
                dialogInterface.dismiss()
            }
            .create()
        if (dialog.window != null)
            dialog.window?.attributes?.windowAnimations = R.style.SlidingDialogAnimation

        dialog.show()
    }

    interface AttendEventsItemClick{
        fun deleteAttendEvent(attendEvent: AttendEventsData, position: Int)
    }
}