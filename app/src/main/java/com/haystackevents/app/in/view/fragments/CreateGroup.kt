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
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.databinding.FragmentCreateGroupBinding
import com.haystackevents.app.`in`.manager.SessionManager
import com.haystackevents.app.`in`.network.repository.Repository
import com.haystackevents.app.`in`.network.response.create_group.Group
import com.haystackevents.app.`in`.network.response.group_members.DefaultResponse
import com.haystackevents.app.`in`.utils.Extensions.hideKeyboard
import com.haystackevents.app.`in`.utils.Extensions.showAlertDialog
import com.haystackevents.app.`in`.utils.Extensions.showSnackBar
import com.haystackevents.app.`in`.utils.ProgressCaller
import com.haystackevents.app.`in`.view.activity.MainMenuActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateGroup: Fragment() {

    private lateinit var binding: FragmentCreateGroupBinding

    private var groupName: String? = null
    private var groupDesc: String? = null
    private var memberName: String? = null
    private var memberEmail: String? = null
    private var memberMobile: String? = null
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateGroupBinding.inflate(layoutInflater)
        return binding.root
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbarCreateGroup.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.constraintCreateGroup.setOnTouchListener { view, motionEvent ->
            when(motionEvent.action){
                KeyEvent.ACTION_UP ->{
                    binding.constraintCreateGroup.hideKeyboard()
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener  false
        }

        binding.btnCreateNewGroup.setOnClickListener {
            if (validated()){
                addNewGroup()
            }
        }

        binding.btnAddressBook.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestContactPermissionResult.launch(Manifest.permission.READ_CONTACTS)
                return@setOnClickListener
            }
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            readContactActivityResult.launch(intent)
        }
    }

    private val requestContactPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { granted ->

        if (granted) {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            readContactActivityResult.launch(intent)
        }
    }

    private val readContactActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode == Activity.RESULT_OK) {

            try {

                val uriContactData: Uri = result.data?.data!!

                val c: Cursor = requireActivity().contentResolver.query(
                    uriContactData,
                    null, null, null, null
                )!!

                var number = ""

                if (c.count >= 1) {
                    if (c.moveToFirst()) {
                        val id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                        val hasPhone = c.getString(c.getColumnIndex(
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
                        val name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

                        binding.inputName.setText(name)
                        binding.inputMobile.setText(number)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("TAG", "exception: ${e.message}")
            }
        }
    }

    private fun addNewGroup() {
        context?.let { ProgressCaller.showProgressDialog(it) }
        userId = SessionManager.instance.getUserId()
        Repository.createNewGroup(groupName!!, groupDesc!!, userId!!).enqueue(object : Callback<Group>{
            override fun onResponse(call: Call<Group>, response: Response<Group>) {
                try {
                    if (response.isSuccessful){
                        if (response.body()?.status == "1"){
                            addMember(response.body()?.groupid.toString())
                        }else{
                            showAlertDialog("Failed", requireContext(), response.body()?.message)
                            ProgressCaller.hideProgressDialog()
                        }
                    }

                }catch (e: Exception){
                    e.printStackTrace()
                    ProgressCaller.hideProgressDialog()
                }
            }

            override fun onFailure(call: Call<Group>, t: Throwable) {
                showSnackBar(binding.constraintCreateGroup, t.localizedMessage!!)
                ProgressCaller.hideProgressDialog()
            }

        })
    }

    private fun addMember(groupId: String?) {
        Repository.addMemberToGroup(groupId!!, userId!!, memberName!!, memberMobile!!, memberEmail!!)
            .enqueue(object : Callback<DefaultResponse>{
                override fun onResponse(
                    call: Call<DefaultResponse>,
                    response: Response<DefaultResponse>
                ) {
                    if (response.isSuccessful){
                        if (response.body()?.status == "1"){
                            showSuccessAlert("Group Created", response.body()?.message!!)
                        }else{
                            showAlertDialog("Failed", requireContext(), response.body()?.message)
                        }
                    }
                    ProgressCaller.hideProgressDialog()
                }

                override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                    showSnackBar(binding.constraintCreateGroup, t.localizedMessage!!)
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

    private fun validated(): Boolean {
        groupName = binding.inputGroupName.text.toString().trim()
        groupDesc = binding.inputGroupDesc.text.toString().trim()
        memberName = binding.inputName.text.toString().trim()
        memberEmail = binding.inputEmail.text.toString().trim()
        memberMobile = binding.inputMobile.text.toString().trim()

        when {
            groupName!!.isEmpty() -> {
                binding.inputGroupName.requestFocus()
                binding.inputGroupName.error = "Enter Group Name"
                return false
            }
            groupDesc!!.isEmpty() -> {
                binding.inputGroupDesc.requestFocus()
                binding.inputGroupDesc.error = "Enter Group Description"
                return false
            }
            memberName!!.isEmpty() -> {
                binding.inputName.requestFocus()
                binding.inputName.error = "Enter Member Name"
                return false
            }
            memberEmail!!.isEmpty() -> {
                binding.inputEmail.requestFocus()
                binding.inputEmail.error = "Enter Member Email"
                return false
            }
            memberMobile!!.isEmpty() -> {
                binding.inputMobile.requestFocus()
                binding.inputMobile.error = "Enter Member Mobile"
                return false
            }
            else -> return true
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainMenuActivity).hideBottomNav()
    }
}