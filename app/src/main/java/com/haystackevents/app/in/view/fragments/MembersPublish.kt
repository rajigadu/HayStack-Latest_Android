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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.haystackevents.app.`in`.network.response.event.EventCreated
import com.haystackevents.app.`in`.network.response.event.Event
import com.haystackevents.app.`in`.utils.AppConstants.ARG_SERIALIZABLE
import com.haystackevents.app.`in`.utils.AppConstants.POSITION
import com.haystackevents.app.`in`.utils.AppConstants.STATUS
import com.haystackevents.app.`in`.utils.Extensions.showAlertDialog
import com.haystackevents.app.`in`.utils.Extensions.showErrorResponse
import com.haystackevents.app.`in`.view.activity.MainMenuActivity
import com.haystackevents.app.`in`.view.adapters.NewlyAddedMembersAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MembersPublish: Fragment(), NewlyAddedMembersAdapter.MembersClickEventListener {


    private lateinit var binding: FragmentMembersPublishBinding
    private lateinit var bottomSheet: BottomSheetDialog
    private lateinit var addedMembersAdapter: NewlyAddedMembersAdapter
    private var events: Event? = null
    private var listMembers = arrayListOf<AllMembers>()

    private var editTextFullName: EditText? = null
    private var editTextMobile: EditText? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMembersPublishBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initiateView()

        clickListeners()

    }

    private fun clickListeners() {

        binding.toolbarMembersPublish.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.toolbarMembersPublish.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.addMember -> {
                    showAddMemberBottomSheet()
                    true
                }
                else -> false
            }

        }

        binding.btnPublish.setOnClickListener {
            showBottomSheet()
            getEventLatLong()
        }
    }

    private fun initiateView() {

        events = arguments?.getSerializable(ARG_SERIALIZABLE) as Event
        Log.e("TAG", "events: $events")

        listMembers.clear()
        if (events!!.allmembers.size > 0){
            listMembers.addAll(events!!.allmembers)
        }

        addedMembersAdapter = NewlyAddedMembersAdapter()
        binding.recyclerMembers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = addedMembersAdapter
            addedMembersAdapter.update(requireContext(), listMembers, this@MembersPublish)
        }

        binding.refreshMembers.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        )
        binding.refreshMembers.setOnRefreshListener {
            listMembers.clear()
            if (events!!.allmembers.size > 0){
                listMembers.addAll(events!!.allmembers)
            }
            addedMembersAdapter.update(requireContext(),listMembers, this)
            binding.refreshMembers.isRefreshing = false
        }
    }

    private fun showAddMemberBottomSheet() {
        val bottomSheet = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.add_member_bottom_sheet, null)
        bottomSheet.setContentView(view)
        bottomSheet.setCancelable(false)
        bottomSheet.show()

        val toolbarBottomSheet = view.findViewById<MaterialToolbar>(R.id.toolbarAddMemberBottomSheet)
        editTextFullName = view.findViewById(R.id.inputName)
        val editTextEmail = view.findViewById<EditText>(R.id.inputEmail)
        editTextMobile = view.findViewById(R.id.inputMobile)
        val btnInvite = view.findViewById<MaterialButton>(R.id.btnInvite)
        val btnChooseFromGroup = view.findViewById<MaterialButton>(R.id.btnAddFromGroup)
        val btnChooseFromContacts = view.findViewById<MaterialButton>(R.id.btnChooseFromAddressBook)

        toolbarBottomSheet.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.actionClose -> {
                    bottomSheet.hide()
                    listMembers.clear()
                    if (events!!.allmembers.size > 0){
                        listMembers.addAll(events!!.allmembers)
                    }
                    context?.let { it1 -> addedMembersAdapter.update(it1,listMembers, this) }
                    return@setOnMenuItemClickListener true
                }
            }
            return@setOnMenuItemClickListener false
        }

        btnInvite.setOnClickListener {
            val fullName = editTextFullName?.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val mobile = editTextMobile?.text.toString().trim()

            if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(mobile)) {
                Toast.makeText(requireContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            events?.allmembers?.add(AllMembers(fullName, email, mobile))

            editTextFullName?.setText("")
            editTextEmail.setText("")
            editTextMobile?.setText("")

        }

        btnChooseFromContacts.setOnClickListener {
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

                        editTextFullName?.setText(name)
                        editTextMobile?.setText(number)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("TAG", "exception: ${e.message}")
            }
        }
    }

    private fun getEventLatLong() {
        val geoCoder = Geocoder(requireContext())
        var listAddress = listOf<Address>()
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
            hideBottomSheet()
        }

        publishCreatedEvent()
    }

    private fun publishCreatedEvent() {
        Log.e("TAG", "events:: $events")
        Repository.createNewEvent(events!!, requireContext()).enqueue(object : Callback<EventCreated>{
            override fun onResponse(call: Call<EventCreated>, response: Response<EventCreated>) {
                Log.e("TAG", "response: "+response.body())
                try {
                    if (response.isSuccessful){
                        if (response.body()?.status == "1"){

                            showSuccessAlert("Event created", response.body()?.message!!)

                        }else{
                            showAlertDialog("Error Occurred!", requireContext(), response.body()?.message!!)
                        }
                    }

                }catch (e: Exception){e.printStackTrace()}
                hideBottomSheet()
            }

            override fun onFailure(call: Call<EventCreated>, t: Throwable) {
                Log.e("TAG", "error: "+t.localizedMessage)
                showErrorResponse(t, binding.constraintPublishEvent)
                hideBottomSheet()
            }

        })
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

    @SuppressLint("SetTextI18n")
    private fun showBottomSheet(){
        bottomSheet = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(requireContext())
            .inflate(
                R.layout.authentication_progress_bottom_sheet,
                requireActivity().findViewById<ConstraintLayout>(R.id.bottom_sheet)
            )

        val title = view.findViewById<TextView>(R.id.progress_title)
        val subTitle = view.findViewById<TextView>(R.id.progress_sub_title)

        title.text = "Event Creating"
        subTitle.text = "Created Event Uploading, Please wait..."

        bottomSheet.setCancelable(false)
        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    private  fun hideBottomSheet(){
        bottomSheet.hide()
    }

    override fun removeMember(position: Int) {
        listMembers.removeAt(position)
        events?.allmembers?.removeAt(position)
        addedMembersAdapter.notifyItemChanged(position)
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