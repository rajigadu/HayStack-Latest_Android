package com.haystackevents.app.`in`.view.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.databinding.ActivityLoginBinding
import com.haystackevents.app.`in`.manager.SessionManager
import com.haystackevents.app.`in`.network.repository.Repository
import com.haystackevents.app.`in`.network.response.login.LogIn
import com.haystackevents.app.`in`.utils.Extensions.getDeviceUid
import com.haystackevents.app.`in`.utils.Extensions.showAlertDialog
import com.haystackevents.app.`in`.utils.Extensions.showErrorResponse
import com.haystackevents.app.`in`.utils.ProgressCaller
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LogInActivity: AppCompatActivity() {

    private var binding: ActivityLoginBinding? = null
    private lateinit var bottomSheet: BottomSheetDialog
    var userName: String? = null
    var password: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        binding?.signup?.setOnClickListener {
            startActivity(Intent(this@LogInActivity, SignUpActivity::class.java))
            finish()
        }

        binding?.forgotPass?.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
            finish()
        }

        binding?.signIn?.setOnClickListener {
            userName = binding?.inputEditTextEmail?.text.toString().trim()
            password = binding?.inputEditTextPassword?.text.toString().trim()

            if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(password)) {
                showAlertDialog("Enter Valid Credential?",this, "Please enter a valid username and password")
                return@setOnClickListener
            }

            validateUserCredentials()
        }
    }

    private fun validateUserCredentials() {
        val deviceId = getDeviceUid(this)
        ProgressCaller.showProgressDialog(this)
        Repository.userLogIn(userName!!, password!!, deviceId).enqueue(
            object : Callback<LogIn>{
                override fun onResponse(call: Call<LogIn>, response: Response<LogIn>) {
                    Log.e("TAG","response: "+response.body())
                    try {

                        if (response.isSuccessful){
                            if (response.body()!!.status == "1"){

                                SessionManager.instance.saveUserCredentials(response.body()!!.data)
                                startActivity(Intent(this@LogInActivity, MainMenuActivity::class.java))
                                finish()

                            }else{
                                showAlertDialog("LogIn Error?",this@LogInActivity,
                                    response.body()?.message ?: "Some Error Occurred, Please try again later")
                            }
                        }

                    }catch (e: Exception){e.printStackTrace()}

                    ProgressCaller.hideProgressDialog()
                }

                override fun onFailure(call: Call<LogIn>, t: Throwable) {
                    showErrorResponse(t, binding?.constraintLogin)
                    ProgressCaller.hideProgressDialog()
                }

            })
    }
}