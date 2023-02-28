package com.haystackevents.app.`in`.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent.ACTION_UP
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.databinding.ActivitySoldierRegistrationBinding
import com.haystackevents.app.`in`.network.repository.Repository
import com.haystackevents.app.`in`.network.response.countries.Countries
import com.haystackevents.app.`in`.network.response.soldier_signup.SignUpResponse
import com.haystackevents.app.`in`.network.response.states.States
import com.haystackevents.app.`in`.utils.Extensions
import com.haystackevents.app.`in`.utils.Extensions.hideKeyboard
import com.haystackevents.app.`in`.utils.Extensions.longSnackBar
import com.haystackevents.app.`in`.utils.Extensions.showAlertDialog
import com.haystackevents.app.`in`.utils.Extensions.showErrorResponse
import com.haystackevents.app.`in`.utils.ProgressCaller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity: AppCompatActivity() {

    private var binding: ActivitySoldierRegistrationBinding? = null
    private var bottomSheet: BottomSheetDialog? = null
    private var fName: String? = null
    private var lName: String? = null
    private var email: String? = null
    private var address: String? = null
    private var number: String? = null
    private var zipcode: String? = null
    private var password: String? = null
    private var country: String? = null
    private var state: String? = null
    private var city: String? = null
    private var accountType: String? = null

    private var listCountries = arrayListOf<String>()
    private var listStates = arrayListOf<String>()

    private var selectedCountry: String? = ""
    private var selectedState: String? = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySoldierRegistrationBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        clickListeners()

        lifecycleScope.launch(Dispatchers.Main) {
            getCountryList()
        }

    }

    private fun getCountryList() {
        ProgressCaller.showProgressDialog(this)
        Repository.getAllCountries().enqueue(object : Callback<Countries>{
            override fun onResponse(call: Call<Countries>, response: Response<Countries>) {
                try {

                    if (response.isSuccessful){
                        if (response.body()?.status == "1"){
                            if (response.body()?.data?.size!! > 0){
                                listCountries.clear()
                                for (elements in response.body()?.data!!){
                                    listCountries.add(elements.name)
                                }
                            }
                        }
                    }

                }catch (e: Exception){e.printStackTrace()}
                ProgressCaller.hideProgressDialog()
            }

            override fun onFailure(call: Call<Countries>, t: Throwable) {
                showErrorResponse(t, binding?.constraintSingUp)
                ProgressCaller.hideProgressDialog()
            }

        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun clickListeners() {

        binding?.signin?.setOnClickListener {
            startActivity(Intent(this, LogInActivity::class.java))
        }

        binding?.forgotPass?.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding?.constraintEditLayout?.setOnTouchListener { view, motionEvent ->
            when(motionEvent.action){
                ACTION_UP ->{
                    binding?.constraintEditLayout?.hideKeyboard()
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener  false
        }

        binding?.btnSignUp?.setOnClickListener {
            if (validated()){
                if (email?.contains("@") == true){
                    completeSoldierRegistration()
                }else{
                    showAlertDialog(
                        "Email not valid?", this,
                        "Please enter a valid email address"
                    )
                }
            }
        }

        binding?.inputCountry?.setOnClickListener {
            showCountriesListDialogView()
        }

        binding?.inputState?.setOnClickListener {
            showStatesListDialog()
        }

        binding?.accTypeCompany?.setOnCheckedChangeListener { compoundButton, isCheckd ->
            if (isCheckd) {
                //accountType = "Company/Small Business"
                binding?.accTypeIndividual?.isChecked = false
            }
        }

        binding?.accTypeIndividual?.setOnCheckedChangeListener { compoundButton, isCheckd ->
            if (isCheckd) {
                //accountType = "Individual"
                binding?.accTypeCompany?.isChecked = false
            }
        }

    }

    private fun completeSoldierRegistration() {
        ProgressCaller.showProgressDialog(this)
        Repository.soldierRegistration(fName!!, lName!!, email!!, address!!, zipcode!!,
            password!!, number!!, country!!, state!!, city!!, accountType!!)
            .enqueue(object : Callback<SignUpResponse>{
                override fun onResponse(
                    call: Call<SignUpResponse>,
                    response: Response<SignUpResponse>
                ) {
                    try {

                        if (response.isSuccessful){
                            if (response.body()?.status == "1"){

                                showSuccessAlert("Registration Success", response.body()?.message!!)

                            }else{
                                showAlertDialog("Sign up Error?", this@SignUpActivity, response.body()?.message)
                            }
                        }

                    }catch (e: Exception){e.printStackTrace()}
                    ProgressCaller.hideProgressDialog()
                }

                override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                    showErrorResponse(t, binding?.constraintSingUp)
                    ProgressCaller.hideProgressDialog()
                }

            })
    }

    private fun showSuccessAlert(title: String, message: String) {
        val dialog = MaterialAlertDialogBuilder(this, R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Ok") { dialogInterface, i ->
                dialogInterface.dismiss()
                startActivity(Intent(this, LogInActivity::class.java))
                finish()
            }
            .create()
        if (dialog.window != null)
            dialog.window?.attributes?.windowAnimations = R.style.SlidingDialogAnimation

        dialog.show()
    }

    private fun validated(): Boolean {
        fName = binding?.inputFirstName?.text.toString().trim()
        lName = binding?.inputLastName?.text.toString().trim()
        email = binding?.inputEmail?.text.toString().trim()
        number = binding?.inputNumber?.text.toString().trim()
        password = binding?.inputPassword?.text.toString().trim()
        address = binding?.inputAddress?.text.toString().trim()
        city = binding?.inputCity?.text.toString().trim()
        zipcode = binding?.inputZipcode?.text.toString().trim()
        country = binding?.inputCountry?.text.toString().trim()
        state = binding?.inputState?.text.toString().trim()

        accountType = if (binding?.accTypeCompany?.isChecked == true) "Company/Small Business"
        else if (binding?.accTypeIndividual?.isChecked == true) "Individual" else ""

        when {
            fName?.isEmpty() == true -> {
                binding?.inputFirstName?.requestFocus()
                binding?.inputFirstName?.error = "Enter First Name"
                return false
            }
            lName?.isEmpty() == true -> {
                binding?.inputLastName?.requestFocus()
                binding?.inputLastName?.error = "Enter Last Name"
                return false
            }
            email?.isEmpty() == true -> {
                binding?.inputEmail?.requestFocus()
                binding?.inputEmail?.error = "Enter email address"
                return false
            }
            number!!.isEmpty() -> {
                binding?.inputNumber?.requestFocus()
                binding?.inputNumber?.error = "Enter Pphone number"
                return false
            }
            password?.isEmpty() == true -> {
                binding?.inputPassword?.requestFocus()
                binding?.inputPassword?.error = "Enter Password"
                return false
            }
            address?.isEmpty() == true -> {
                binding?.inputAddress?.requestFocus()
                binding?.inputAddress?.error = "Enter address"
                return false
            }
            country?.isEmpty() == true -> {
                longSnackBar("Please select country", binding?.constraintSingUp)
                return false
            }
            state?.isEmpty() == true -> {
                longSnackBar("Please select state", binding?.constraintSingUp)
                return false
            }
            city?.isEmpty() == true -> {
                binding?.inputCity?.requestFocus()
                binding?.inputCity?.error = "Enter City name"
                return false
            }
            zipcode?.isEmpty() == true -> {
                binding?.inputZipcode?.requestFocus()
                binding?.inputZipcode?.error = "Enter zipcode"
                return false
            }
            accountType.isNullOrEmpty() -> {
                binding?.constraintSingUp.let {
                    Extensions.showSnackBar(it,"Please select account type")
                }
                return false
            }

            else -> return true
        }
    }

    private fun showStatesListDialog() {
        var array = arrayOf<String>()
        array = listStates.toArray(array)

        MaterialAlertDialogBuilder(this, R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle("Select Event State")
            .setCancelable(false)
            .setPositiveButton("Ok"){ dialog, which ->
                binding?.inputState?.text = selectedState
            }
            .setSingleChoiceItems(array,-1){ dialog, which ->
                selectedState = array[which]
            }
            .show()
    }

    private fun showCountriesListDialogView() {
        var array = arrayOf<String>()
        array = listCountries.toArray(array)

        MaterialAlertDialogBuilder(this, R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle("Select Event Country")
            .setCancelable(false)
            .setPositiveButton("Ok"){ dialog, which ->
                getStatesList()
                binding?.inputCountry?.text = selectedCountry
                selectedState = null
                binding?.inputState?.text = ""
            }
            .setSingleChoiceItems(array,-1){ dialog, which ->
                selectedCountry = array[which]
            }
            .show()
    }

    private fun getStatesList() {
        ProgressCaller.showProgressDialog(this)
        Repository.getAllStatesOfTheCountry(selectedCountry!!).enqueue(
            object : Callback<States>{
                override fun onResponse(call: Call<States>, response: Response<States>) {
                    try {
                        if (response.isSuccessful){
                            if (response.body()?.status == "1"){
                                if (response.body()?.data?.isNotEmpty() == true){
                                    listStates.clear()
                                    for (item in response.body()?.data!!){
                                        listStates.add(item.name)
                                    }
                                }
                            }
                        }

                    }catch (e: Exception){e.printStackTrace()}
                    ProgressCaller.hideProgressDialog()
                }

                override fun onFailure(call: Call<States>, t: Throwable) {
                    showErrorResponse(t, binding?.constraintSingUp)
                    ProgressCaller.hideProgressDialog()
                }

            })
    }

}