package com.haystackevents.app.`in`.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.databinding.FragmentMembersBinding
import com.haystackevents.app.`in`.manager.SessionManager
import com.haystackevents.app.`in`.network.repository.Repository
import com.haystackevents.app.`in`.network.response.group_members.Data
import com.haystackevents.app.`in`.network.response.group_members.DefaultResponse
import com.haystackevents.app.`in`.network.response.group_members.GroupMembers
import com.haystackevents.app.`in`.utils.AppConstants.FROM_ADD_MEMBERS_FRAGMENT
import com.haystackevents.app.`in`.utils.AppConstants.GROUP_ID
import com.haystackevents.app.`in`.utils.AppConstants.MEMBER_EMAIL
import com.haystackevents.app.`in`.utils.AppConstants.MEMBER_ID
import com.haystackevents.app.`in`.utils.AppConstants.MEMBER_NAME
import com.haystackevents.app.`in`.utils.AppConstants.MEMBER_PHONE
import com.haystackevents.app.`in`.utils.AppConstants.STATUS
import com.haystackevents.app.`in`.utils.Extensions.showAlertDialog
import com.haystackevents.app.`in`.utils.Extensions.showSnackBar
import com.haystackevents.app.`in`.utils.RecyclerViewCustomAnimation
import com.haystackevents.app.`in`.view.activity.MainMenuActivity
import com.haystackevents.app.`in`.view.adapters.MembersListAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MembersFragment: Fragment(), MembersListAdapter.MembersListItemClick {


    private lateinit var binding: FragmentMembersBinding
    private lateinit var membersAdapter: MembersListAdapter
    private var listMembers = arrayListOf<Data>()
    private var groupId: String? = null
    private var navigateFrom: Boolean? = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMembersBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupId = arguments?.getString(GROUP_ID)
        navigateFrom = arguments?.getBoolean(FROM_ADD_MEMBERS_FRAGMENT)

        binding.refreshMembersList.setColorSchemeColors(ContextCompat.getColor(requireContext(),
            R.color.colorPrimary))

        binding.refreshMembersList.setOnRefreshListener {
            listMembers.clear()
            getGroupMembersList()
        }

        membersAdapter = MembersListAdapter(requireContext(), this)
        binding.recyclerViewMembers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = membersAdapter
            itemAnimator = RecyclerViewCustomAnimation()
        }

        binding.toolbarMembers.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.toolbarMembers.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.addMember -> {
                    val bundle = bundleOf(
                        STATUS to "0",
                        GROUP_ID to groupId
                    )
                    findNavController().navigate(R.id.action_membersFragment_to_editMember, bundle)
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
    }

    private fun getGroupMembersList() {
        binding.refreshMembersList.isRefreshing = true
        Repository.getGroupMembers(groupId!!, SessionManager.instance.getUserId())
            .enqueue(object : Callback<GroupMembers>{
                override fun onResponse(
                    call: Call<GroupMembers>,
                    response: Response<GroupMembers>
                ) {
                    try {

                        if (response.isSuccessful){
                            if (response.body()?.status == "1"){
                                if (response.body()?.data?.size!! > 0){
                                    listMembers.clear()
                                    listMembers.addAll(response.body()?.data!!)
                                    membersAdapter.update(listMembers)
                                }

                            }else{
                                showAlertDialog("Failed!", requireContext(), response.body()?.message)
                            }
                        }

                    }catch (e: Exception){e.printStackTrace()}

                    binding.refreshMembersList.isRefreshing = false
                }

                override fun onFailure(call: Call<GroupMembers>, t: Throwable) {
                    showSnackBar(binding.constraintMembers, t.localizedMessage!!)
                    binding.refreshMembersList.isRefreshing = false
                }

            })
    }

    override fun onResume() {
        super.onResume()
        getGroupMembersList()
        (activity as MainMenuActivity).hideBottomNav()
    }

    override fun editMember(groupId: String, name: String, email: String, phone: String, memberId: String) {
        //val bundle = bundleOf(GROUP_ID to groupId)
        val bundle = bundleOf(
            STATUS to "1",
            GROUP_ID to groupId,
            MEMBER_NAME to name,
            MEMBER_EMAIL to email,
            MEMBER_PHONE to phone,
            MEMBER_ID to memberId
        )
        findNavController().navigate(R.id.action_membersFragment_to_editMember, bundle)
    }

    override fun deleteMember(groupId: String, memberId: String) {
        Repository.deleteGroupMember(groupId, memberId, SessionManager.instance.getUserId())
            .enqueue(object : Callback<DefaultResponse>{
                override fun onResponse(
                    call: Call<DefaultResponse>,
                    response: Response<DefaultResponse>
                ) {

                    try {

                        if (response.isSuccessful){
                            if (response.body()?.status == "1"){

                                showSnackBar(binding.constraintMembers, response.body()?.message!!)

                                getGroupMembersList()

                            }else{
                                showAlertDialog("Failed!", requireContext(), response.body()?.message)
                            }
                        }

                    }catch (e: Exception){e.printStackTrace()}
                }

                override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                    showSnackBar(binding.constraintMembers, t.localizedMessage!!)
                }

            })
    }

    override fun memberItemSelected(data: Data) {
        if (navigateFrom == true) {
            activity?.supportFragmentManager?.setFragmentResult(
                "fragment-callback", bundleOf("data" to data)
            )
            findNavController().navigate(R.id.action_membersFragment_to_addMembersFragment)
        }
    }
}