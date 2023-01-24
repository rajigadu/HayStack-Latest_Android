package com.haystackevents.app.`in`.view.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.haystackevents.app.`in`.databinding.FragmentContactUsBinding
import com.haystackevents.app.`in`.network.repository.Repository
import com.haystackevents.app.`in`.network.response.group_members.DefaultResponse
import com.haystackevents.app.`in`.utils.Extensions.showAlertDialog
import com.haystackevents.app.`in`.utils.Extensions.showSnackBar
import com.haystackevents.app.`in`.utils.ProgressCaller
import com.haystackevents.app.`in`.view.activity.MainMenuActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ContactUs: Fragment() {


    private var binding: FragmentContactUsBinding? = null

    private var fullName: String? = null
    private var nameOrEmail: String? = null
    private var description: String? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentContactUsBinding.inflate(layoutInflater)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.toolbarContactUs?.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding?.btnUpdate?.setOnClickListener {
            fullName = binding?.inputEditTextFullName?.text.toString().trim()
            nameOrEmail = binding?.inputEditTextEmail?.text.toString().trim()
            description = binding?.inputDesc?.text.toString().trim()

            if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(nameOrEmail) || TextUtils.isEmpty(description)){
                showSnackBar(binding?.constraintContactUs, "Please enter all fields")
                return@setOnClickListener
            }

            contactUs()
        }

    }

    private fun contactUs(){
        context?.let { ProgressCaller.showProgressDialog(it) }
        Repository.contactUs(fullName!!, nameOrEmail!!, description!!).enqueue(object :
        Callback<DefaultResponse>{
            override fun onResponse(
                call: Call<DefaultResponse>,
                response: Response<DefaultResponse>
            ) {
                try {

                    if (response.isSuccessful){
                        if (response.body()?.status == "1"){

                            showAlertDialog("Success", requireContext(), response.body()?.message)
                            binding?.inputEditTextEmail?.setText("")
                            binding?.inputEditTextFullName?.setText("")
                            binding?.inputDesc?.setText("")

                        }else{
                            showAlertDialog("Failed!", requireContext(), response.body()?.message)
                        }
                    }
                    ProgressCaller.hideProgressDialog()

                }catch (e: Exception){e.printStackTrace()}
            }

            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                showSnackBar(binding?.constraintContactUs, t.localizedMessage!!)
                ProgressCaller.hideProgressDialog()
            }

        })
    }

    override fun onResume() {
        super.onResume()
        (activity as MainMenuActivity).hideBottomNav()
    }
}