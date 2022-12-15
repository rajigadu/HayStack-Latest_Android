package com.haystackevents.app.`in`.view.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.databinding.LayoutGroupsListItemViewBinding
import com.haystackevents.app.`in`.network.response.all_groups.Data
import com.haystackevents.app.`in`.view.fragments.GroupsFragment

class EventListAdapter(val context: Context, val fragment: GroupsFragment)
    : RecyclerView.Adapter<EventListAdapter.ViewHolder>() {

    private lateinit var groupItemClick: EventGroupItemClickListener
    private var listGroups = arrayListOf<Data>()


    inner class ViewHolder(val binding: LayoutGroupsListItemViewBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bindView(data: Data) {
            binding.eventName.text = data.gname

            if (data.membercount.toInt() < 1){
                binding.membersCount.text = "Add Member"
                binding.membersLayout.visibility = INVISIBLE
                binding.addMember.visibility = VISIBLE
            }else {
                binding.membersLayout.visibility = VISIBLE
                binding.addMember.visibility = INVISIBLE
                binding.membersCount.text = "People (${data.membercount})"

                var membersCount = data.membercount.toInt()
                if (membersCount > 3){
                    membersCount = 3
                }

                binding.membersLayout.removeAllViews()
                for (element in 0 until membersCount) {
                    val text = TextView(context)
                    text.text = data.member[element].member.substring(0,1)
                    text.layoutParams = ViewGroup.LayoutParams(70, 70)
                    text.gravity = Gravity.CENTER
                    text.setTextColor(ContextCompat.getColor(context, R.color.white))
                    text.typeface = ResourcesCompat.getFont(context, R.font.lato_bold)
                    text.background = ContextCompat.getDrawable(context, R.drawable.rounded_text_bag)
                    binding.membersLayout.addView(text)
                }
            }


            binding.editEventGroup.setOnClickListener {
                groupItemClick.groupItemEdit(data.id)
            }

            binding.membersLayout.setOnClickListener {
                groupItemClick.membersViewClick(data.id)
            }

            binding.eventItem.setOnClickListener {
                groupItemClick.membersViewClick(data.id)
            }

            binding.deleteGroup.setOnClickListener {
                showConfirmAlertDialog(data.id)
            }
        }
    }

    private fun showConfirmAlertDialog(id: String) {
        val dialog = MaterialAlertDialogBuilder(context, R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle("Delete Group?")
            .setMessage("Are you sure want to delete this group.?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialogInterface, i ->
                dialogInterface.dismiss()
                groupItemClick.deleteGroup(id)
            }
            .setNegativeButton("No") { dialogInterface, i ->
                dialogInterface.dismiss()
            }
            .create()
        if (dialog.window != null)
            dialog.window?.attributes?.windowAnimations = R.style.SlidingDialogAnimation

        dialog.show()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventListAdapter.ViewHolder =
        ViewHolder(
            LayoutGroupsListItemViewBinding.inflate(LayoutInflater.from(
            parent.context
        ), parent, false))

    override fun onBindViewHolder(holder: EventListAdapter.ViewHolder, position: Int) {
        groupItemClick = fragment
        holder.bindView(listGroups[position])
    }

    fun updateGroupList(groupList: ArrayList<Data>){
        this.listGroups = groupList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = listGroups.size

    interface EventGroupItemClickListener{
        fun groupItemEdit(groupId: String)
        fun membersViewClick(groupId: String)
        fun deleteGroup(groupId: String)
    }
}