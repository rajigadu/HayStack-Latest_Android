 package com.haystackevents.app.`in`.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.manager.SessionManager

 private const val SPLASH_DELAY: Long = 3000

 class SplashScreen : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        initView()
    }

     private fun initView() {
         SessionManager.init(this)
         Handler(Looper.getMainLooper()).postDelayed(mRunnable, SPLASH_DELAY)
     }

     private val mRunnable = Runnable {
         if (!isFinishing){
             if (SessionManager.instance.getUserId()?.isNotEmpty() == true){

                 startActivity(Intent(this, MainMenuActivity::class.java))
                 finish()

             }else {
                 startActivity(Intent(this, LogInActivity::class.java))
                 finish()
             }
         }
     }
 }