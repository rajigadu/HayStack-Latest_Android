package com.haystackevents.app.`in`.view.activity

import android.content.IntentSender
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.databinding.ActivityMainMenuBinding
import com.haystackevents.app.`in`.utils.AppConstants.REQ_CODE_VERSION_UPDATE

class MainMenuActivity: AppCompatActivity() {

    private var binding: ActivityMainMenuBinding? = null
    private lateinit var navController: NavHostController
    private lateinit var appUpdateManager: AppUpdateManager
    private var installStateUpdatedListener: InstallStateUpdatedListener? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        appUpdateManager = AppUpdateManagerFactory.create(this)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
        navController = navHostFragment.findNavController() as NavHostController
        navController.navigateUp()

        binding?.navHome?.setOnClickListener {
            navController.navigate(R.id.homeFragment)
        }

        binding?.navProfile?.setOnClickListener {
            navController.navigate(R.id.profile)
        }

        binding?.navRefer?.setOnClickListener {
            navController.navigate(R.id.referAFriend)
        }

        binding?.createEvent?.setOnClickListener {
            navController.navigate(R.id.createEvent)
        }

        binding?.navEvents?.setOnClickListener {
            navController.navigate(R.id.groupsFragment)
        }

        checkAppUpdate()
    }

    fun updateBottomNavChange(position: Int){
        when(position){
            0 -> {
                binding?.navHome?.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary))
                binding?.navProfile?.setColorFilter(ContextCompat.getColor(this, R.color.colorDefaultBottomNavIcon))
                binding?.navRefer?.setColorFilter(ContextCompat.getColor(this, R.color.colorDefaultBottomNavIcon))
                binding?.navEvents?.setColorFilter(ContextCompat.getColor(this, R.color.colorDefaultBottomNavIcon))
                binding?.createEvent?.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorDefaultBottomNavIcon))
            }
            1 -> {
                binding?.navHome?.setColorFilter(ContextCompat.getColor(this, R.color.colorDefaultBottomNavIcon))
                binding?.navProfile?.setColorFilter(ContextCompat.getColor(this, R.color.colorDefaultBottomNavIcon))
                binding?.navRefer?.setColorFilter(ContextCompat.getColor(this, R.color.colorDefaultBottomNavIcon))
                binding?.navEvents?.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary))
                binding?.createEvent?.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorDefaultBottomNavIcon))
            }
            2 -> {
                binding?.navHome?.setColorFilter(ContextCompat.getColor(this, R.color.colorDefaultBottomNavIcon))
                binding?.navProfile?.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary))
                binding?.navRefer?.setColorFilter(ContextCompat.getColor(this, R.color.colorDefaultBottomNavIcon))
                binding?.navEvents?.setColorFilter(ContextCompat.getColor(this, R.color.colorDefaultBottomNavIcon))
                binding?.createEvent?.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorDefaultBottomNavIcon))
            }
            3 -> {
                binding?.navHome?.setColorFilter(ContextCompat.getColor(this, R.color.colorDefaultBottomNavIcon))
                binding?.navProfile?.setColorFilter(ContextCompat.getColor(this, R.color.colorDefaultBottomNavIcon))
                binding?.navRefer?.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary))
                binding?.navEvents?.setColorFilter(ContextCompat.getColor(this, R.color.colorDefaultBottomNavIcon))
                binding?.createEvent?.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorDefaultBottomNavIcon))
            }
            4 -> {
                binding?.navHome?.setColorFilter(ContextCompat.getColor(this, R.color.colorDefaultBottomNavIcon))
                binding?.navProfile?.setColorFilter(ContextCompat.getColor(this, R.color.colorDefaultBottomNavIcon))
                binding?.navRefer?.setColorFilter(ContextCompat.getColor(this, R.color.colorDefaultBottomNavIcon))
                binding?.navEvents?.setColorFilter(ContextCompat.getColor(this, R.color.colorDefaultBottomNavIcon))
                binding?.createEvent?.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimary))
            }
        }
    }

    fun hideBottomNav(){
        binding?.customBottomNavigation?.visibility = GONE
    }

    fun showBottomNav(){
        binding?.customBottomNavigation?.visibility = VISIBLE
    }

    private fun checkAppUpdate(){
        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        // Checks that the platform will allow the specified type of update.
        //Log.e("TAG", "Checking for updates")

        installStateUpdatedListener = InstallStateUpdatedListener { installState ->
            if (installState.installStatus() == InstallStatus.DOWNLOADED)
            // After the update is downloaded, show a notification
            // and request user confirmation to restart the app.
                popupSnackBarForCompleteUpdateAndUnregister()
        }

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                // Request the update.
                if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {

                    // Before starting an update, register a listener for updates.
                    appUpdateManager.registerListener(installStateUpdatedListener!!)
                    // Start an update.
                    startAppUpdateFlexible(appUpdateInfo)
                } else if (appUpdateInfo.isUpdateTypeAllowed(IMMEDIATE) ) {
                    // Start an update.
                    startAppUpdateImmediate(appUpdateInfo)
                }
            }
        }
    }

    private fun popupSnackBarForCompleteUpdateAndUnregister() {
        val snackbar = binding?.constraintMain?.let {
            Snackbar.make(
                it,
                getString(R.string.update_downloaded),
                Snackbar.LENGTH_INDEFINITE
            )
        }
        snackbar?.setAction(
            R.string.restart
        ) { appUpdateManager.completeUpdate() }
        snackbar?.setActionTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        snackbar?.show()

        unregisterInstallStateUpdListener()
    }

    private fun unregisterInstallStateUpdListener() {
        if (installStateUpdatedListener != null)
            appUpdateManager.unregisterListener(installStateUpdatedListener!!)
    }

    private fun startAppUpdateImmediate(appUpdateInfo: AppUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                IMMEDIATE,  // The current activity making the update request.
                this,  // Include a request code to later monitor this update request.
                REQ_CODE_VERSION_UPDATE
            )
        } catch (e: IntentSender.SendIntentException) {
            e.printStackTrace()
        }
    }

    private fun startAppUpdateFlexible(appUpdateInfo: AppUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.FLEXIBLE,  // The current activity making the update request.
                this,  // Include a request code to later monitor this update request.
                REQ_CODE_VERSION_UPDATE
            )
        } catch (e: IntentSender.SendIntentException) {
            e.printStackTrace()
            unregisterInstallStateUpdListener()
        }
    }

    private fun checkNewAppVersionState() {
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
                //FLEXIBLE:
                // If the update is downloaded but not installed,
                // notify the user to complete the update.
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    popupSnackBarForCompleteUpdateAndUnregister()
                }

                //IMMEDIATE:
                if (appUpdateInfo.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    // If an in-app update is already running, resume the update.
                    startAppUpdateImmediate(appUpdateInfo)
                }
            }

    }

    override fun onDestroy() {
        unregisterInstallStateUpdListener()
        //clearSharedPrefData()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        checkNewAppVersionState()
    }

}
