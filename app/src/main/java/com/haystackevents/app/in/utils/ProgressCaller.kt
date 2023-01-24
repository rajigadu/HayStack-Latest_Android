package com.haystackevents.app.`in`.utils


import android.app.Application
import android.app.Dialog
import android.content.Context
import android.view.Window
import com.haystackevents.app.`in`.R
import java.lang.Exception

object ProgressCaller : Application() {

    private var dialog: Dialog? = null

    fun showProgressDialog(context: Context){

        try {

            dialog = Dialog(context)
            dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)

            if (dialog != null) {
                dialog?.setContentView(R.layout.dialog_view)
                dialog?.setCancelable(false)
                if (dialog?.isShowing != true)
                    dialog?.show()
                else dialog?.dismiss()
            } else {
                dialog = Dialog(context)
            }
        }
        catch (e: Exception) {
            dialog?.dismiss()
        }
    }

    fun hideProgressDialog(){
        try {
            dialog?.dismiss()
            dialog?.hide()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}