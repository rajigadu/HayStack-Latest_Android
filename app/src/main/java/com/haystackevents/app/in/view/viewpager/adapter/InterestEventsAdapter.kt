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
import com.haystackevents.app.`in`.view.viewpager.InterestsEventsFragment
import java.util.*

class InterestEventsAdapter(var context: Context)
    : RecyclerView.Adapter<InterestEventsAdapter.ViewHolder>() {

    private var listInterestEvents = arrayListOf<InterestEventsData>()
    private lateinit var eventOnClick: InterestedEventsItemClick



    inner class ViewHolder(val binding: LayoutGroupsListItemViewBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bindView(interestEvents: InterestEventsData) {
            binding.deleteGroup.visibility = View.INVISIBLE
            binding.eventName.text = interestEvents.event_name

            binding.deleteGroup.setOnClickListener {
                showConfirmationDialog(interestEvents)
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
        holder.bindView(listInterestEvents[position])
    }

    override fun getItemCount(): Int = listInterestEvents.size

    fun update(listMyEvents: ArrayList<InterestEventsData>, onClick: InterestsEventsFragment){
        this.listInterestEvents = listMyEvents
        this.eventOnClick = onClick
        notifyDataSetChanged()
    }

    private fun showConfirmationDialog(interestEvents: InterestEventsData) {
        val dialog = MaterialAlertDialogBuilder(context, R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle("Delete Event?")
            .setMessage("Are you sure want to delete this event.?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialogInterface, i ->
                dialogInterface.dismiss()
                eventOnClick.deleteInterestedEvent(interestEvents)
            }
            .setNegativeButton("No") { dialogInterface, i ->
                dialogInterface.dismiss()
            }
            .create()
        if (dialog.window != null)
            dialog.window?.attributes?.windowAnimations = R.style.SlidingDialogAnimation

        dialog.show()
    }

    interface InterestedEventsItemClick{
        fun deleteInterestedEvent(interestEvent: InterestEventsData)
    }
}