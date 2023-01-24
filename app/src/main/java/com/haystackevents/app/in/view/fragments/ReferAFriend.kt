package com.haystackevents.app.`in`.view.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.haystackevents.app.`in`.databinding.FragmentReferFriendBinding
import com.haystackevents.app.`in`.manager.SessionManager
import com.haystackevents.app.`in`.network.repository.Repository
import com.haystackevents.app.`in`.network.response.group_members.DefaultResponse
import com.haystackevents.app.`in`.utils.Extensions
import com.haystackevents.app.`in`.utils.Extensions.hideKeyboard
import com.haystackevents.app.`in`.utils.Extensions.showAlertDialog
import com.haystackevents.app.`in`.utils.ProgressCaller
import com.haystackevents.app.`in`.view.activity.MainMenuActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReferAFriend: Fragment() {

    private var binding: FragmentReferFriendBinding? = null
    private var name: String? = null
    private var email: String? = null
    private var number: String? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReferFriendBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.btnReferNow?.setOnClickListener {
            if (validate()) {
                binding?.constraintReferFriend?.hideKeyboard()
                referFriendNow()
            }
        }
    }

    private fun validate(): Boolean {
        name = binding?.inputName?.text.toString().trim()
        email = binding?.inputEmail?.text.toString().trim()
        number = binding?.inputMobile?.text.toString().trim()

        if (TextUtils.isEmpty(name)) {
            binding?.inputName?.error = "Name Should not be empty"
            return false
        }
        if (TextUtils.isEmpty(number)) {
            binding?.inputMobile?.error = "Mobile Number> Should not be empty"
            return false
        }
        if (TextUtils.isEmpty(email)) {
            binding?.inputEmail?.error = "Email Should not be empty"
            return false
        }
        return true
    }

    private fun referFriendNow() {
        context?.let { ProgressCaller.showProgressDialog(it) }
        Repository.referFriendNow(
            id = SessionManager.instance.getUserId(),
            name = name!!,
            email = email!!,
            number = number!!
        ).enqueue(object : Callback<DefaultResponse>{
            override fun onResponse(
                call: Call<DefaultResponse>,
                response: Response<DefaultResponse>
            ) {
                context?.let { context ->
                    showAlertDialog(
                        "Refer Your Friend",
                        message = response.body()?.data?.message,
                        context = context
                    )
                }
                ProgressCaller.hideProgressDialog()
                clearFields()
            }

            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                Extensions.showSnackBar(binding?.constraintReferFriend, t.localizedMessage!!)
                ProgressCaller.hideProgressDialog()
            }
        })
    }

    private fun clearFields() {
        binding?.inputName?.setText("")
        binding?.inputEmail?.setText("")
        binding?.inputMobile?.setText("")
    }

    override fun onResume() {
        super.onResume()
        (activity as MainMenuActivity).updateBottomNavChange(3)
        (activity as MainMenuActivity).showBottomNav()
    }
}