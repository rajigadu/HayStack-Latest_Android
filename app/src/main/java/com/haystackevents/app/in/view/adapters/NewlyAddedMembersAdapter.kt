package com.haystackevents.app.`in`.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.databinding.LayoutMembersListItemViewBinding
import com.haystackevents.app.`in`.network.response.event.AllMembers
import com.haystackevents.app.`in`.view.fragments.MembersPublish
import java.util.ArrayList

class NewlyAddedMembersAdapter: RecyclerView.Adapter<NewlyAddedMembersAdapter.ViewHolder>() {

    private var listAddedMembers = arrayListOf<AllMembers>()
    private var context: Context? = null
    private lateinit var memberListener: MembersClickEventListener


    inner class ViewHolder(val binding: LayoutMembersListItemViewBinding): RecyclerView.ViewHolder(binding.root) {
        fun bindView(allMembers: AllMembers) {
            binding.memberName.text = allMembers.member
            binding.memberPhone.text = allMembers.number
            binding.memberEmail.text = allMembers.email

            binding.deleteMember.setOnClickListener {
                showConfirmationDialog(adapterPosition)
            }

            binding.editMember.setOnClickListener {

            }
        }
    }

    private fun showConfirmationDialog(adapterPosition: Int) {
        val dialog = MaterialAlertDialogBuilder(context!!, R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle("Delete Member?")
            .setMessage("Are you sure want to delete this member.?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialogInterface, i ->
                dialogInterface.dismiss()
                memberListener.removeMember(adapterPosition)
            }
            .setNegativeButton("No") { dialogInterface, i ->
                dialogInterface.dismiss()
            }
            .create()
        if (dialog.window != null)
            dialog.window?.attributes?.windowAnimations = R.style.SlidingDialogAnimation

        dialog.show()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewlyAddedMembersAdapter.ViewHolder =
        ViewHolder(LayoutMembersListItemViewBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: NewlyAddedMembersAdapter.ViewHolder, position: Int) {
        holder.bindView(listAddedMembers[position])
    }

    override fun getItemCount(): Int = listAddedMembers.size

    fun update(requireContext: Context, listMembers: ArrayList<AllMembers>, listener: MembersPublish) {
        this.context = requireContext
        this.listAddedMembers = listMembers
        this.memberListener = listener
        notifyDataSetChanged()
    }

    interface MembersClickEventListener{
        fun removeMember(position: Int)
        fun editMember(position: Int)
    }
}