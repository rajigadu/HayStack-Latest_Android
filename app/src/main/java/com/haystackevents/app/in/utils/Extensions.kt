package com.haystackevents.app.`in`.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.haystackevents.app.`in`.R
import com.yalantis.ucrop.util.FileUtils.*
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object Extensions {


    fun showSnackBarTop(view: View, context: Context, message: String) {
        val snackBarView = Snackbar.make(view, message , Snackbar.LENGTH_LONG)
        val mView = snackBarView.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        //mView.layoutParams = params
        //view.background = ContextCompat.getDrawable(context,R.drawable.custom_drawable) // for custom background
        snackBarView.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
        snackBarView.show()
    }

    fun showSnackBar(view: View?, message: String) =
        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_LONG).show() }

    fun View.hideKeyboard() {
        val inputMethodManager = context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }

    fun showAlertDialog(title: String, context: Context, message: String?){

        val dialog = MaterialAlertDialogBuilder(context, R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Ok") { dialogInterface, i ->
                dialogInterface.dismiss()
            }
            .create()
        if (dialog.window != null)
            dialog.window?.attributes?.windowAnimations = R.style.SlidingDialogAnimation

        dialog.show()
    }

    fun showSnackBarSettings(
        context: Context,
        mainTextStringId: String, actionStringId: String,
        listener: View.OnClickListener) {
        Toast.makeText(context, mainTextStringId, Toast.LENGTH_LONG).show()
    }

    @SuppressLint("SimpleDateFormat")
    fun convertedDateFormat(date: String?): String? {
        Log.e("TAG", "date: $date")
        var formattedDate: Date? = null
        var convertDate: String? = null
        val sdf = SimpleDateFormat("dd MMM yyyy")
        try {
            formattedDate = date?.let { sdf.parse(it) }
            convertDate = formattedDate?.let { SimpleDateFormat("mm-dd-yyyy").format(it) }

        }catch (e: Exception){e.printStackTrace()}
        return convertDate
    }

    @SuppressLint("HardwareIds")
    fun getDeviceUid(context: Context): String{
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    @SuppressLint("SimpleDateFormat")
    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("MM-dd-yyyy")
        return sdf.format(Date())
    }

    @SuppressLint("SimpleDateFormat")
    fun getCurrentTime(): String {
        var timeState = "AM"
        val calendar = Calendar.getInstance()
        var hour = calendar.get(Calendar.HOUR_OF_DAY).toString()
        var minute = calendar.get(Calendar.MINUTE).toString()

        if (hour.toInt() > 11) timeState = "PM"
        if (hour.toInt() > 12) hour = (hour.toInt() -12).toString()

        if (hour.length == 1) hour = "0$hour"
        if (minute.length == 1) minute = "0$minute"
        if (hour == "00") hour = "12"

        return "$hour:$minute $timeState"
    }

    @SuppressLint("SimpleDateFormat")
    fun getUniqueRandomNumber(): String{
        var random = ""
        val sdf = SimpleDateFormat("yyyyMMdd")
        val currentDate = sdf.format(Date())

        random = currentDate + Random().nextInt(1000000000).toString()

        return random
    }

    fun longSnackBar(message: String, view: View?) =
        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_LONG).show() }

    fun showErrorResponse(throwable: Throwable, constraintLogin: View?) {
        when(throwable){
            is IOException -> {
                longSnackBar(throwable.localizedMessage, constraintLogin)
            }
            is HttpException -> {
                val code = throwable.code()
                Log.e("TAG", "code: $code")
                longSnackBar("Some Error occurred, $code!", constraintLogin)
            }
            else -> {
                Log.e("TAG", "error: "+throwable.localizedMessage)
                longSnackBar("Something went wrong, please try again", constraintLogin)
            }
        }
    }

    fun getRealPathUri(context: Context, imageUri: Uri): String?{
        var result: String? = ""
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        try {
            val cursor: Cursor = context.contentResolver.query(
                imageUri,
                filePathColumn, null, null, null)!!
            cursor.moveToFirst()

            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            result = cursor.getString(columnIndex)
            cursor.close()
        }catch (e: java.lang.Exception){}
        Log.e("TAG", "real path uri: $result")
        return result
    }


    @SuppressLint("ObsoleteSdkInt")
    fun getRealPathFromURIAPI(context: Context?, uri: Uri): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                when (type) {
                    "image" -> {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                    "video" -> {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                    "audio" -> {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    "document" -> {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        }

        else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment
            else getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    val constraintsBuilder =
        CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())
}