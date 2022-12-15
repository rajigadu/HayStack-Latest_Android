package com.haystackevents.app.`in`.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.databinding.FragmentGroupsBinding
import com.haystackevents.app.`in`.manager.SessionManager
import com.haystackevents.app.`in`.network.repository.Repository
import com.haystackevents.app.`in`.network.response.all_groups.AllGroups
import com.haystackevents.app.`in`.network.response.all_groups.Data
import com.haystackevents.app.`in`.network.response.group_members.DefaultResponse
import com.haystackevents.app.`in`.utils.AppConstants
import com.haystackevents.app.`in`.utils.AppConstants.FROM_ADD_MEMBERS_FRAGMENT
import com.haystackevents.app.`in`.utils.AppConstants.GROUP_ID
import com.haystackevents.app.`in`.utils.Extensions.showAlertDialog
import com.haystackevents.app.`in`.utils.Extensions.showSnackBar
import com.haystackevents.app.`in`.utils.RecyclerViewCustomAnimation
import com.haystackevents.app.`in`.view.activity.MainMenuActivity
import com.haystackevents.app.`in`.view.adapters.EventListAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GroupsFragment: Fragment(), EventListAdapter.EventGroupItemClickListener {


    private lateinit var binding: FragmentGroupsBinding
    private lateinit var eventListAdapter: EventListAdapter
    private var listGroups = arrayListOf<Data>()
    private var from: Boolean? = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGroupsBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.refreshGroupList.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.colorPrimary))

        binding.refreshGroupList.setOnRefreshListener {
            listGroups.clear()
            getAllGroups()
        }

        binding.toolbarGroups.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.addMember -> {
                    findNavController().navigate(R.id.action_groupsFragment_to_createGroup)
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }

        eventListAdapter = EventListAdapter(requireContext(), this)
        binding.recyclerEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventListAdapter
            itemAnimator = RecyclerViewCustomAnimation()
        }

        binding.btnCreateGroup.setOnClickListener {
            findNavController().navigate(R.id.action_groupsFragment_to_createGroup)
        }

    }

    private fun getAllGroups() {
        binding.refreshGroupList.isRefreshing = true
        Repository.getAllGroupsList(SessionManager.instance.getUserId())
            .enqueue(object : Callback<AllGroups>{
                override fun onResponse(call: Call<AllGroups>, response: Response<AllGroups>) {
                    try {
                        if (response.isSuccessful){
                            if (response.body()?.status == "1"){
                                if (response.body()?.data?.size!! > 0){
                                    showGroupList()
                                    listGroups.clear()
                                    listGroups.addAll(response.body()?.data!!)
                                    eventListAdapter.updateGroupList(listGroups)
                                }else{
                                    showEmptyGroup()
                                }

                            }else{
                                if (response.body()?.status == "0") {
                                    showEmptyGroup()
                                } else {
                                    showAlertDialog(
                                        "Some Error Occurred!",
                                        requireContext(),
                                        response.body()?.message
                                    )
                                }
                            }
                        }

                    }catch (e: Exception){e.printStackTrace()}

                    binding.refreshGroupList.isRefreshing = false
                }

                override fun onFailure(call: Call<AllGroups>, t: Throwable) {
                    showSnackBar(binding.constraintGroups, t.localizedMessage!!)
                    binding.refreshGroupList.isRefreshing = false
                }

            })
    }

    private fun showEmptyGroup(){
        binding.refreshGroupList.visibility = INVISIBLE
        binding.btnCreateGroup.visibility = VISIBLE
        binding.emptyGroups.visibility = VISIBLE
    }

    private fun showGroupList(){
        binding.refreshGroupList.visibility = VISIBLE
        binding.btnCreateGroup.visibility = INVISIBLE
        binding.emptyGroups.visibility = INVISIBLE
    }

    override fun onResume() {
        super.onResume()
        getAllGroups()
        from = arguments?.getBoolean(FROM_ADD_MEMBERS_FRAGMENT, false)
        if (from == true){
            (activity as? MainMenuActivity)?.hideBottomNav()
        } else {
            (activity as? MainMenuActivity)?.updateBottomNavChange(1)
            (activity as? MainMenuActivity)?.showBottomNav()
        }
    }

    override fun groupItemEdit(groupId: String) {
        val bundle = bundleOf(GROUP_ID to groupId)
        findNavController().navigate(R.id.action_groupsFragment_to_editGroup, bundle)
    }

    override fun membersViewClick(groupId: String) {
        val bundle = bundleOf(
            GROUP_ID to groupId,
            FROM_ADD_MEMBERS_FRAGMENT to from
        )
        findNavController().navigate(R.id.action_groupsFragment_to_membersFragment, bundle)
    }

    override fun deleteGroup(groupId: String) {
        binding.refreshGroupList.isRefreshing = true
        Repository.deleteGroup(groupId, SessionManager.instance.getUserId())
            .enqueue(object : Callback<DefaultResponse>{
                override fun onResponse(
                    call: Call<DefaultResponse>,
                    response: Response<DefaultResponse>
                ) {
                    try {

                        if (response.isSuccessful){
                            if (response.body()?.status == "1"){

                                showSnackBar(binding.constraintGroups, response.body()?.message!!)

                                getAllGroups()

                            }else{
                                showAlertDialog("Failed!", requireContext(), response.body()?.message)
                            }
                        }

                    }catch (e: Exception){e.printStackTrace()}
                    binding.refreshGroupList.isRefreshing = false
                }

                override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                    showSnackBar(binding.constraintGroups, t.localizedMessage!!)
                    binding.refreshGroupList.isRefreshing = false
                }

            })
    }
}