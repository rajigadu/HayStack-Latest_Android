package com.haystackevents.app.`in`.view.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent.ACTION_UP
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.databinding.FragmentEditMemberBinding
import com.haystackevents.app.`in`.manager.SessionManager
import com.haystackevents.app.`in`.network.repository.Repository
import com.haystackevents.app.`in`.network.response.event.Event
import com.haystackevents.app.`in`.network.response.group_members.DefaultResponse
import com.haystackevents.app.`in`.utils.AppConstants.ARG_SERIALIZABLE
import com.haystackevents.app.`in`.utils.AppConstants.GROUP_ID
import com.haystackevents.app.`in`.utils.AppConstants.MEMBER_EMAIL
import com.haystackevents.app.`in`.utils.AppConstants.MEMBER_ID
import com.haystackevents.app.`in`.utils.AppConstants.MEMBER_NAME
import com.haystackevents.app.`in`.utils.AppConstants.MEMBER_PHONE
import com.haystackevents.app.`in`.utils.AppConstants.POSITION
import com.haystackevents.app.`in`.utils.AppConstants.STATUS
import com.haystackevents.app.`in`.utils.Extensions
import com.haystackevents.app.`in`.utils.Extensions.hideKeyboard
import com.haystackevents.app.`in`.utils.Extensions.showSnackBar
import com.haystackevents.app.`in`.utils.ProgressCaller
import com.haystackevents.app.`in`.view.activity.MainMenuActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditMember: Fragment() {

    private var binding: FragmentEditMemberBinding? = null
    private var status: String? = null
    private var groupId: String? = null
    private var name: String? = null
    private var email: String? = null
    private var phone: String? = null
    private var events: Event? = null
    private var position: Int? = null
    private var memberId: String? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditMemberBinding.inflate(layoutInflater)
        return binding?.root
    }


    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupId = arguments?.getString(GROUP_ID)
        status =  arguments?.getString(STATUS)
        memberId =  arguments?.getString(MEMBER_ID)
        when (status) {
            "1" -> {
                name = arguments?.getString(MEMBER_NAME)
                email = arguments?.getString(MEMBER_EMAIL)
                phone = arguments?.getString(MEMBER_PHONE)

                binding?.inputName?.setText(name)
                binding?.inputEmail?.setText(email)
                binding?.inputMobile?.setText(phone)
            }
            "2" -> {
                events = arguments?.getSerializable(ARG_SERIALIZABLE) as? Event
                position = arguments?.getInt(POSITION)

                binding?.inputName?.setText(events?.allmembers!![position!!].member)
                binding?.inputEmail?.setText(events?.allmembers!![position!!].email)
                binding?.inputMobile?.setText(events?.allmembers!![position!!].number)
            }
            else -> {
                binding?.btnUpdate?.text = "Add Member"
            }
        }

        binding?.toolbarEditMember?.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding?.constraintEditMember?.setOnTouchListener { view, motionEvent ->
            when(motionEvent.action){
                ACTION_UP -> {
                    binding?.constraintEditMember?.hideKeyboard()
                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }
        }

        binding?.btnAddressBook?.setOnClickListener {
            activity?.let { activity ->
                if (ContextCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.READ_CONTACTS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissionResult.launch(Manifest.permission.READ_CONTACTS)
                    return@setOnClickListener
                } else {
                    val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                    readContactActivityResult.launch(intent)
                }
            }
        }

        binding?.btnUpdate?.setOnClickListener {
            name = binding?.inputName?.text.toString().trim()
            email = binding?.inputEmail?.text.toString().trim()
            phone = binding?.inputMobile?.text.toString().trim()

            if (TextUtils.isEmpty(name)) {
                showSnackBar(binding?.constraintEditMember, "Please Enter Name")
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(email) && TextUtils.isEmpty(phone)) {
                showSnackBar(binding?.constraintEditMember, "Email or Phone is mandatory")
                return@setOnClickListener
            }

            when (status) {
                "1" -> updateMemberDetails()
                "2" -> updateMemberList()
                else -> addNewMember()
            }
        }
    }

    private val requestPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            readContactActivityResult.launch(intent)
        }
    }

    private val readContactActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val uriContactData: Uri? = result.data?.data
                val cursor: Cursor? = uriContactData?.let {
                    activity?.contentResolver?.query(
                        it, null, null, null, null
                    )
                }
                var number = ""
                cursor?.count?.let { count ->
                    if (count >= 1) {
                        if (cursor.moveToFirst()) {
                            val id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                            val hasPhone = cursor.getString(cursor.getColumnIndex(
                                ContactsContract.Contacts.HAS_PHONE_NUMBER))

                            if (hasPhone.equals("1", true)) {
                                val phones: Cursor = requireActivity().contentResolver.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                    null, null
                                )!!
                                phones.moveToFirst()
                                number = phones.getString(phones.getColumnIndex("data1"))
                            }
                            val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

                            binding?.inputName?.setText(name)
                            binding?.inputMobile?.setText(number)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("TAG", "exception: ${e.message}")
            }
        }
    }

    private fun updateMemberList() {
        events?.allmembers!![position!!].number = phone!!
        events?.allmembers!![position!!].email = email!!
        events?.allmembers!![position!!].member = name!!
    }

    private fun addNewMember() {
        context?.let { ProgressCaller.showProgressDialog(it) }
        val userId = SessionManager.instance.getUserId()
        Repository.addMemberToGroup(groupId!!, userId, name!!, phone!!, email!!)
            .enqueue(object : Callback<DefaultResponse> {
                override fun onResponse(
                    call: Call<DefaultResponse>,
                    response: Response<DefaultResponse>
                ) {
                    if (response.isSuccessful){
                        if (response.body()?.status == "1"){
                            showSuccessAlert("Member Added", response.body()?.message!!)
                        }else{
                            Extensions.showAlertDialog(
                                "Failed",
                                requireContext(),
                                response.body()?.message
                            )
                        }
                    }
                    ProgressCaller.hideProgressDialog()
                }

                override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                    showSnackBar(binding?.constraintEditMember, t.localizedMessage!!)
                    ProgressCaller.hideProgressDialog()
                }

            })
    }

    private fun updateMemberDetails() {
        context?.let { ProgressCaller.showProgressDialog(it) }
        Repository.editGroupMember(groupId!!, name!!, phone!!, email!!, memberId!!)
            .enqueue(object: Callback<DefaultResponse>{
                override fun onResponse(
                    call: Call<DefaultResponse>,
                    response: Response<DefaultResponse>
                ) {
                    if (response.isSuccessful){
                        if (response.body()?.status == "1"){

                            showSuccessAlert("Member Updated", response.body()?.message!!)

                        }else{
                            Extensions.showAlertDialog(
                                "Failed",
                                requireContext(),
                                response.body()?.message
                            )
                        }
                    }
                    ProgressCaller.hideProgressDialog()
                }

                override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                    showSnackBar(binding?.constraintEditMember, t.localizedMessage!!)
                    ProgressCaller.hideProgressDialog()
                }
            })
    }

    private fun showSuccessAlert(title: String, message: String) {
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Ok") { dialogInterface, i ->
                dialogInterface.dismiss()
                findNavController().popBackStack()
            }
            .create()
        if (dialog.window != null)
            dialog.window?.attributes?.windowAnimations = R.style.SlidingDialogAnimation

        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        (activity as MainMenuActivity).hideBottomNav()
    }
}