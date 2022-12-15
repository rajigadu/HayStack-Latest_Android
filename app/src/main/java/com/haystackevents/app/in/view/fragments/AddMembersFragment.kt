package com.haystackevents.app.`in`.view.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.databinding.FragmentAddMemberBinding
import com.haystackevents.app.`in`.network.response.event.AllMembers
import com.haystackevents.app.`in`.network.response.event.Event
import com.haystackevents.app.`in`.network.response.group_members.Data
import com.haystackevents.app.`in`.utils.AppConstants.ARG_SERIALIZABLE
import com.haystackevents.app.`in`.utils.AppConstants.FROM_ADD_MEMBERS_FRAGMENT
import com.haystackevents.app.`in`.utils.Extensions.showSnackBar
import com.haystackevents.app.`in`.view.activity.MainMenuActivity


class AddMembersFragment: Fragment() {

    private lateinit var binding: FragmentAddMemberBinding
    private var events: Event? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddMemberBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        events = arguments?.getSerializable(ARG_SERIALIZABLE) as? Event
        //Log.e("TAG", "events: $events")
        activity?.supportFragmentManager?.setFragmentResultListener(
            "fragment-callback", viewLifecycleOwner) { key, bundle ->
            val data = bundle.getSerializable("data") as? Data
            binding.inputMobile.setText(data?.member)
            binding.inputName.setText(data?.number)
            binding.inputEmail.setText(data?.email)
        }

        binding.toolbarAddMember.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnInvite.setOnClickListener {
            val fullName = binding.inputName.text.toString().trim()
            val email = binding.inputEmail.text.toString().trim()
            val mobile = binding.inputMobile.text.toString().trim()

            if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(mobile)) {
                showSnackBar(binding.constraintAddMember, "Please fill all the fields")
                return@setOnClickListener
            }
            //events?.allmembers?.clear()
            events?.allmembers?.add(AllMembers(fullName, email, mobile))

            val bundle = bundleOf(ARG_SERIALIZABLE to events)
            findNavController().navigate(R.id.action_addMembersFragment_to_membersPublish, bundle)
        }

        binding.btnSkip.setOnClickListener {
            val bundle = bundleOf(ARG_SERIALIZABLE to events)
            findNavController().navigate(
                R.id.action_addMembersFragment_to_membersPublish,
                bundle
            )
        }

        binding.btnChooseFromAddressBook.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionResult.launch(Manifest.permission.READ_CONTACTS)
                return@setOnClickListener
            }
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            readContactActivityResult.launch(intent)
        }

        binding.btnAddFromGroup.setOnClickListener {
            val bundle = bundleOf(FROM_ADD_MEMBERS_FRAGMENT to true)
            findNavController().navigate(
                R.id.action_addMembersFragment_to_groupsFragment,
                bundle
            )
        }
    }

    private val requestPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { granted ->

        if (granted) {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            readContactActivityResult.launch(intent)
        }
    }

    private val readContactActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode == RESULT_OK) {

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

    override fun onResume() {
        super.onResume()
        (activity as MainMenuActivity).hideBottomNav()
    }
}