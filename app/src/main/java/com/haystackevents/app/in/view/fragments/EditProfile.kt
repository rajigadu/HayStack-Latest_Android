package com.haystackevents.app.`in`.view.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.haystackevents.app.`in`.databinding.FragmentEditProfileBinding
import com.haystackevents.app.`in`.manager.SessionManager
import com.haystackevents.app.`in`.network.repository.Repository
import com.haystackevents.app.`in`.network.response.group_members.DefaultResponse
import com.haystackevents.app.`in`.utils.Extensions.showAlertDialog
import com.haystackevents.app.`in`.utils.Extensions.showSnackBar
import com.haystackevents.app.`in`.view.activity.MainMenuActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditProfile: Fragment() {

    private lateinit var binding: FragmentEditProfileBinding
    private var firstName: String? = null
    private var lastName: String? = null
    private var userNumber: String? = null
    private var logniedUser: String? = null
    private var userId: String? = null



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbarEditProfile.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnUpdate.setOnClickListener {
            firstName = binding.inputEditTextFirstName.text.toString().trim()
            lastName = binding.inputEditTextLastName.text.toString().trim()
            userNumber = binding.inputEditTextUserNumber.text.toString().trim()

            if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(userNumber)){
                showSnackBar(binding.constraintEditProfile, "Please fill all fields")
                return@setOnClickListener
            }

            updateEditProfile()
        }
    }

    private fun updateEditProfile() {
        userId = SessionManager.instance.getUserId()
        logniedUser = SessionManager.instance.getLoginUser()
        binding.progressUpdate.visibility = VISIBLE

        Repository.editProfile(firstName!!, lastName!!, userNumber!!, userId!!)
            .enqueue(object : Callback<DefaultResponse>{
                override fun onResponse(
                    call: Call<DefaultResponse>,
                    response: Response<DefaultResponse>
                ) {
                    try {

                        if (response.isSuccessful){
                            if (response.body()?.status == "1"){
                                binding.inputEditTextFirstName.setText("")
                                binding.inputEditTextLastName.setText("")
                                binding.inputEditTextUserNumber.setText("")
                                showAlertDialog("Success", requireContext(), response.body()?.message)

                            }else{
                                showAlertDialog("Failed!", requireContext(), response.body()?.message)
                            }
                        }

                    }catch (e: Exception){e.printStackTrace()}
                    binding.progressUpdate.visibility = INVISIBLE
                }

                override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                    showSnackBar(binding.constraintEditProfile, t.localizedMessage!!)
                    binding.progressUpdate.visibility = INVISIBLE
                }

            })
    }

    override fun onResume() {
        super.onResume()
        (activity as MainMenuActivity).hideBottomNav()
    }
}