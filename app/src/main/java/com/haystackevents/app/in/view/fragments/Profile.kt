package com.haystackevents.app.`in`.view.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.haystackevents.app.`in`.BuildConfig
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.databinding.FragmentProfileBinding
import com.haystackevents.app.`in`.manager.SessionManager
import com.haystackevents.app.`in`.view.activity.LogInActivity
import com.haystackevents.app.`in`.view.activity.MainMenuActivity

class Profile: Fragment() {


    private var binding: FragmentProfileBinding? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        return binding?.root
    }


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.layoutLogout?.setOnClickListener {
            showConfirmationDialog()
        }

        binding?.layoutChangePassword?.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_changePassword)
        }

        binding?.layoutContactUs?.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_contactUs)
        }

        binding?.layoutEditProfile?.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_editProfile)
        }

        binding?.layoutTermsAndConditions?.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_termsAndConditions)
        }

        binding?.appVersion?.text = "v${BuildConfig.VERSION_NAME}"

    }

    private fun showConfirmationDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle("Logout")
            .setMessage(" Are you sure want to logout.?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialogInterface, i ->
                dialogInterface.dismiss()
                SessionManager.instance.clearSessionData()
                startActivity(Intent(requireContext(), LogInActivity::class.java))
                requireActivity().finish()
            }
            .setNegativeButton("No"){ dialogInterface, i ->
                dialogInterface.dismiss()
            }
            .create()
        if (dialog.window != null)
            dialog.window?.attributes?.windowAnimations = R.style.SlidingDialogAnimation

        dialog.show()
    }


    override fun onResume() {
        super.onResume()
        (activity as MainMenuActivity).updateBottomNavChange(2)
        (activity as MainMenuActivity).showBottomNav()
    }
}