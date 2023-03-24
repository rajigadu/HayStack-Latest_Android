package com.haystackevents.app.`in`.view.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.databinding.FragmentMembersPublishBinding
import com.haystackevents.app.`in`.network.repository.Repository
import com.haystackevents.app.`in`.network.response.event.AllMembers
import com.haystackevents.app.`in`.network.response.event.Event
import com.haystackevents.app.`in`.network.response.event.EventCreated
import com.haystackevents.app.`in`.network.response.group_members.Data
import com.haystackevents.app.`in`.utils.AppConstants
import com.haystackevents.app.`in`.utils.AppConstants.ARG_SERIALIZABLE
import com.haystackevents.app.`in`.utils.AppConstants.POSITION
import com.haystackevents.app.`in`.utils.AppConstants.STATUS
import com.haystackevents.app.`in`.utils.Extensions.showAlertDialog
import com.haystackevents.app.`in`.utils.Extensions.showErrorResponse
import com.haystackevents.app.`in`.utils.ProgressCaller
import com.haystackevents.app.`in`.view.activity.MainMenuActivity
import com.haystackevents.app.`in`.view.adapters.NewlyAddedMembersAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MembersPublish: Fragment(), NewlyAddedMembersAdapter.MembersClickEventListener {


    private var binding: FragmentMembersPublishBinding? = null
    private var addedMembersAdapter: NewlyAddedMembersAdapter? = null
    private var events: Event? = null
    private var listMembers = arrayListOf<AllMembers>()

    private var editTextFullName: EditText? = null
    private var editTextMobile: EditText? = null
    private var editTextEmail: EditText? = null
    private var bottomSheet: BottomSheetDialog? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMembersPublishBinding.inflate(layoutInflater)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clickListeners()

        if (arguments?.getString("flow") == "1") {
            events = arguments?.getSerializable(ARG_SERIALIZABLE) as? Event
            //Log.e("TAG", "eventss: $events")
            initiateView()
        } else {
            activity?.supportFragmentManager?.setFragmentResultListener(
                "fragment-callback2", viewLifecycleOwner
            ) { key, bundle ->
                events = bundle.getSerializable(ARG_SERIALIZABLE) as? Event
                //Log.e("TAG", "eventss: $events")
                showAddMemberBottomSheet()
                val data = bundle.getSerializable("data") as? Data
                editTextFullName?.setText(data?.member)
                editTextMobile?.setText(data?.number)
                editTextEmail?.setText(data?.email)
                initiateView()
            }
        }
    }

    private fun clickListeners() {

        binding?.toolbarMembersPublish?.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding?.toolbarMembersPublish?.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.addMember -> {
                    showAddMemberBottomSheet()
                    true
                }
                else -> false
            }

        }

        binding?.btnPublish?.setOnClickListener {
            context?.let { it1 -> ProgressCaller.showProgressDialog(it1) }
            getEventLatLong()
        }

        binding?.btnCancel?.setOnClickListener {
            showEventCancellationConfirmDialog()
        }

        binding?.btnInviteUsers?.setOnClickListener {
            showAddMemberBottomSheet()
        }
    }

    private fun showEventCancellationConfirmDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle("Cancel Event")
            .setMessage("Are you sure want to cancel this event creation?")
            .setCancelable(false)
            .setPositiveButton("Ok") { dialogInterface, i ->
                dialogInterface.dismiss()
                findNavController().navigate(R.id.action_membersPublish_to_homeFragment)
            }
            .setNegativeButton("Cancel") { dialogInterface, i ->
                dialogInterface.dismiss()
            }
            .create()
        if (dialog.window != null)
            dialog.window?.attributes?.windowAnimations = R.style.SlidingDialogAnimation

        dialog.show()
    }

    private fun initiateView() {
        //Log.e("TAG", "events: $events")
        listMembers.clear()
        events?.allmembers?.let { allMembers -> listMembers.addAll(allMembers) }
        addedMembersAdapter = NewlyAddedMembersAdapter()
        binding?.recyclerMembers?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = addedMembersAdapter
            addedMembersAdapter?.update(requireContext(), listMembers, this@MembersPublish)
        }

        binding?.refreshMembers?.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        )
        binding?.refreshMembers?.setOnRefreshListener {
            listMembers.clear()
            events?.allmembers?.let { allMembers -> listMembers.addAll(allMembers) }
            addedMembersAdapter?.update(requireContext(),listMembers, this)
            binding?.refreshMembers?.isRefreshing = false
        }
    }

    private fun showAddMemberBottomSheet() {
        activity?.let { activity ->
            bottomSheet = BottomSheetDialog(activity)
            bottomSheet?.setContentView(R.layout.add_member_bottom_sheet)
            bottomSheet?.setCancelable(false)
            bottomSheet?.show()

            val toolbarBottomSheet = bottomSheet?.findViewById<MaterialToolbar>(R.id.toolbarAddMemberBottomSheet)
            editTextFullName = bottomSheet?.findViewById(R.id.inputName)
            editTextEmail = bottomSheet?.findViewById(R.id.inputEmail)
            editTextMobile = bottomSheet?.findViewById(R.id.inputMobile)
            val btnInvite = bottomSheet?.findViewById<MaterialButton>(R.id.btnInvite)
            val btnChooseFromGroup = bottomSheet?.findViewById<MaterialButton>(R.id.btnAddFromGroup)
            val btnChooseFromContacts = bottomSheet?.findViewById<MaterialButton>(R.id.btnChooseFromAddressBook)

            btnChooseFromGroup?.setOnClickListener {
                bottomSheet?.dismiss()
                val bundle = bundleOf(
                    AppConstants.FROM_ADD_MEMBERS_PUBLISH_FRAGMENT to true,
                    ARG_SERIALIZABLE to events
                )
                findNavController().navigate(
                    R.id.action_membersPublish_to_groupsFragment,
                    bundle
                )
                clearFields()
            }

            toolbarBottomSheet?.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.actionClose -> {
                        bottomSheet?.dismiss()
                        listMembers.clear()
                        events?.allmembers?.let { allMembers -> listMembers.addAll(allMembers) }
                        addedMembersAdapter?.update(activity, listMembers, this)
                        return@setOnMenuItemClickListener true
                    }
                }
                return@setOnMenuItemClickListener false
            }

            btnInvite?.setOnClickListener {
                val fullName = editTextFullName?.text.toString().trim()
                val email = editTextEmail?.text.toString().trim()
                val mobile = editTextMobile?.text.toString().trim()

                if (TextUtils.isEmpty(fullName)) {
                    editTextFullName?.error = "Please Enter Name"
                    return@setOnClickListener
                }
                if (TextUtils.isEmpty(email) && TextUtils.isEmpty(mobile)) {
                    Toast.makeText(activity,"Email or Phone is mandatory",Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                events?.allmembers?.add(AllMembers(fullName, email, mobile))
                clearFields()
            }

            btnChooseFromContacts?.setOnClickListener {
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
                clearFields()
            }
        }
    }

    private fun clearFields() {
        editTextFullName?.setText("")
        editTextMobile?.setText("")
        editTextEmail?.setText("")
    }

    private val requestPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { granted ->

        if (granted) {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            readContactActivityResult.launch(intent)
        }
    }

    @SuppressLint("Range")
    private val readContactActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode == Activity.RESULT_OK) {

            try {

                val uriContactData: Uri = result.data?.data!!

                val cursor: Cursor? = activity?.contentResolver?.query(
                    uriContactData,
                    null, null, null, null
                )

                var number: String? = null

                if ((cursor?.count ?: 0) >= 1) {
                    if (cursor?.moveToFirst() == true) {
                        val contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID))
                        val id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                        val hasPhone = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))

                        if (hasPhone == 1) {
                            val phones: Cursor? = activity?.contentResolver?.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                arrayOf(contactId), null
                            )
                            phones?.moveToFirst()
                            number = phones?.getString(phones.getColumnIndex("data1"))
                        }
                        val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

                        editTextFullName?.setText(name)
                        editTextMobile?.setText(number)
                    }
                }
                cursor?.close()

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("TAG", "exception: ${e.message}")
            }
        }
    }

    private fun getEventLatLong() {
        val geoCoder = Geocoder(requireContext())
        val listAddress: MutableList<Address>?
        val locationName = events?.streetaddress + "," + events?.city + "," + events?.state +
                "," + events?.zipcode

        try {
            listAddress = geoCoder.getFromLocationName(locationName, 5)
            if (listAddress != null){
                val location = listAddress[0]
                events?.latitude = location.latitude.toString()
                events?.longitude = location.longitude.toString()
            }

        }catch (e: Exception){
            e.printStackTrace()
        }
        publishCreatedEvent()
    }

    private fun publishCreatedEvent() {
        Log.e("TAG", "events:: ${events}")
        events?.let {
            context?.let { context ->
                Repository.createNewEvent(it, context).enqueue(object : Callback<EventCreated>{
                    override fun onResponse(call: Call<EventCreated>, response: Response<EventCreated>) {
                        Log.e("TAG", "response: "+response.body())
                        try {
                            if (response.isSuccessful){
                                if (response.body()?.status == "1"){

                                    showSuccessAlert("Event created", response.body()?.message
                                        ?: "Event Created Successfully")

                                }else{
                                    showAlertDialog("Error Occurred!", requireContext(), response.body()?.message
                                        ?: "Oops! Something went wrong")
                                }
                            }

                        }catch (e: Exception){e.printStackTrace()}
                        ProgressCaller.hideProgressDialog()
                    }

                    override fun onFailure(call: Call<EventCreated>, t: Throwable) {
                        Log.e("TAG", "error: "+t.localizedMessage)
                        showErrorResponse(t, binding?.constraintPublishEvent)
                        ProgressCaller.hideProgressDialog()
                    }

                })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainMenuActivity).hideBottomNav()
    }

    private fun showSuccessAlert(title: String, message: String) {
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Ok") { dialogInterface, i ->
                dialogInterface.dismiss()
                findNavController().navigate(R.id.action_membersPublish_to_eventCreated)
            }
            .create()
        if (dialog.window != null)
            dialog.window?.attributes?.windowAnimations = R.style.SlidingDialogAnimation

        dialog.show()
    }

    override fun removeMember(position: Int) {
        listMembers.removeAt(position)
        events?.allmembers?.removeAt(position)
        addedMembersAdapter?.notifyItemChanged(position)
    }

    override fun editMember(position: Int) {
        val bundle = bundleOf(
            STATUS to "2",
            ARG_SERIALIZABLE to events,
            POSITION to position
        )
        findNavController().navigate(R.id.action_membersPublish_to_editMember, bundle)
    }
}