package com.haystackevents.app.`in`.view.viewpager.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.databinding.LayoutGroupsListItemViewBinding
import com.haystackevents.app.`in`.network.response.interest_events.InterestEventsData
import com.haystackevents.app.`in`.network.response.invited_events.InvitedEventsData
import com.haystackevents.app.`in`.view.viewpager.InvitedEventsFragment
import java.util.*

class InvitedEventsAdapter(var context: Context)
    : RecyclerView.Adapter<InvitedEventsAdapter.ViewHolder>() {

    private var listInvitedEvents = arrayListOf<InvitedEventsData>()
    private lateinit var eventOnClick: InvitedEventsItemClick




    inner class ViewHolder(val binding: LayoutGroupsListItemViewBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bindView(invitedEvents: InvitedEventsData) {
            binding.deleteGroup.visibility = View.INVISIBLE
            binding.eventName.text = invitedEvents.event_name

            binding.deleteGroup.setOnClickListener {
                showConfirmationDialog(invitedEvents)
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
        holder.bindView(listInvitedEvents[position])
    }

    override fun getItemCount(): Int = listInvitedEvents.size

    @SuppressLint("NotifyDataSetChanged")
    fun update(listMyEvents: ArrayList<InvitedEventsData>, onClick: InvitedEventsFragment){
        this.listInvitedEvents = listMyEvents
        this.eventOnClick = onClick
        notifyDataSetChanged()
    }

    private fun showConfirmationDialog(invitedEvent: InvitedEventsData) {
        val dialog = MaterialAlertDialogBuilder(context, R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle("Delete Event?")
            .setMessage("Are you sure want to delete this event.?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialogInterface, i ->
                dialogInterface.dismiss()
                eventOnClick.deleteInvitedEvent(invitedEvent)
            }
            .setNegativeButton("No") { dialogInterface, i ->
                dialogInterface.dismiss()
            }
            .create()
        if (dialog.window != null)
            dialog.window?.attributes?.windowAnimations = R.style.SlidingDialogAnimation

        dialog.show()
    }

    interface InvitedEventsItemClick{
        fun deleteInvitedEvent(invitedEvents: InvitedEventsData)
    }
}