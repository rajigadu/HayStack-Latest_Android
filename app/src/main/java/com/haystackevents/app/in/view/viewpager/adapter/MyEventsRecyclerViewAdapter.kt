package com.haystackevents.app.`in`.view.viewpager.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.databinding.LayoutGroupsListItemViewBinding
import com.haystackevents.app.`in`.network.response.my_events.MyEventsData
import com.haystackevents.app.`in`.view.viewpager.MyEventsFragment
import java.util.ArrayList

class MyEventsRecyclerViewAdapter(var context: Context)
    : RecyclerView.Adapter<MyEventsRecyclerViewAdapter.ViewHolder>() {

    private var listMyEvents = arrayListOf<MyEventsData>()
    private var eventsOnClick: MyEventsOnClickListener? = null



    inner class ViewHolder(val binding: LayoutGroupsListItemViewBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bindView(myEvents: MyEventsData) {
            binding.eventName.text = myEvents.event_name
            binding.membersCount.text = "People (${myEvents.membercount})"

            binding.eventItem.setOnClickListener {
                eventsOnClick?.myEventsItemCLick(myEvents)
            }

            binding.editEventGroup.setOnClickListener {
                eventsOnClick?.editMyEvent(myEvents)
            }

            binding.deleteGroup.setOnClickListener {
                showConfirmationDialog(myEvents)
            }
        }
    }

    private fun showConfirmationDialog(myEvents: MyEventsData) {
        val dialog = MaterialAlertDialogBuilder(context, R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle("Delete Event?")
            .setMessage("Are you sure want to delete this event.?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialogInterface, i ->
                dialogInterface.dismiss()
                eventsOnClick?.deleteMyEvent(myEvents)
            }
            .setNegativeButton("No") { dialogInterface, i ->
                dialogInterface.dismiss()
            }
            .create()
        if (dialog.window != null)
            dialog.window?.attributes?.windowAnimations = R.style.SlidingDialogAnimation

        dialog.show()
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
        holder.bindView(listMyEvents[position])
    }

    override fun getItemCount(): Int = listMyEvents.size

    fun update(listMyEvents: ArrayList<MyEventsData>, clickListener: MyEventsFragment){
        this.listMyEvents = listMyEvents
        this.eventsOnClick = clickListener
        notifyDataSetChanged()
    }

    interface MyEventsOnClickListener{
        fun myEventsItemCLick(events: MyEventsData)
        fun deleteMyEvent(events: MyEventsData)
        fun editMyEvent(events: MyEventsData)
    }
}