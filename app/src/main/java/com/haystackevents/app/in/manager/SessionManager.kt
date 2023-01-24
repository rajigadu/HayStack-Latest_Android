package com.haystackevents.app.`in`.manager

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.maps.model.LatLng
import com.haystackevents.app.`in`.network.response.login.LogInData
import com.haystackevents.app.`in`.utils.AppConstants.DOD_ID
import com.haystackevents.app.`in`.utils.AppConstants.EMAIL
import com.haystackevents.app.`in`.utils.AppConstants.FB_TOKEN
import com.haystackevents.app.`in`.utils.AppConstants.F_NAME
import com.haystackevents.app.`in`.utils.AppConstants.GOVT_EMAIL
import com.haystackevents.app.`in`.utils.AppConstants.LOGNIED_USER
import com.haystackevents.app.`in`.utils.AppConstants.L_NAME
import com.haystackevents.app.`in`.utils.AppConstants.MOBILE
import com.haystackevents.app.`in`.utils.AppConstants.UID
import com.haystackevents.app.`in`.utils.AppConstants.USER_ID
import com.haystackevents.app.`in`.utils.AppConstants.USER_LATITUDE
import com.haystackevents.app.`in`.utils.AppConstants.USER_LONGITUDE
import com.haystackevents.app.`in`.utils.AppConstants.USER_NAME

class SessionManager constructor(val context: Context) {


    val sPreference: SharedPreferences
        get() = context.getSharedPreferences("${context.packageName}.session", Context.MODE_PRIVATE)







    fun getUserLatLng(): LatLng = LatLng(
            sPreference.getString(USER_LATITUDE, "")!!.toDouble(),
            sPreference.getString(USER_LONGITUDE, "")!!.toDouble()
        )

    fun getLoginUser(): String = sPreference.getString(LOGNIED_USER, "")!!

    fun getUserId(): String? = sPreference.getString(USER_ID, "")

    fun getUserToken(): String = sPreference.getString(FB_TOKEN, "")!!

    fun getUserFirstName(): String = sPreference.getString(F_NAME, "")!!

    fun getUserLastName(): String = sPreference.getString(L_NAME, "")!!

    fun getUserMail(): String = sPreference.getString(GOVT_EMAIL, "")!!

    fun getUserMobile(): String = sPreference.getString(MOBILE, "")!!

    fun saveProfileData(fName: String?, lName: String?, mobile: String?) {
        val editor = sPreference.edit()

        editor.putString(MOBILE, mobile)
        editor.putString(F_NAME, fName)
        editor.putString(L_NAME, lName)

        editor.apply()
    }

    fun saveUserCredentials(login: List<LogInData>) {
        val editor = sPreference.edit()

        editor.putString(USER_NAME, login[0].username)
        editor.putString(F_NAME, login[0].fname)
        editor.putString(L_NAME, login[0].lname)
        editor.putString(USER_ID, login[0].id)
        editor.putString(EMAIL, login[0].email)
        editor.putString(MOBILE, login[0].mobile)
        editor.putString(MOBILE, login[0].mobile)

        editor.apply()
    }

    fun clearSessionData() = sPreference.edit().clear().apply()

    fun saveUid(uid: String) = sPreference.edit().putString(UID, uid).apply()

    fun saveUserLatLong(latitude: Double, longitude: Double) {
        val editor = sPreference.edit()
        editor.putString(USER_LATITUDE, latitude.toString())
        editor.putString(USER_LONGITUDE, longitude.toString())
        editor.apply()
    }

    fun saveUserFbToken(token: String) {
        val editor = sPreference.edit()
        editor.putString(FB_TOKEN, token)
        editor.apply()
    }


    companion object {

        @SuppressLint("StaticFieldLeak")
        private var mInstance: SessionManager? = null

        fun init(context: Context?) {
            mInstance =
                SessionManager(context!!)
        }

        val instance: SessionManager
            get() {
                if (mInstance == null)
                    throw RuntimeException("Initialize SessionManager")

                return mInstance as SessionManager
            }
    }
}