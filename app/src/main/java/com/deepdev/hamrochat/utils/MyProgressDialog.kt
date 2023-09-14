package com.deepdev.hamrochat.utils

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.deepdev.hamrochat.R

class MyProgressDialog (val context : Context) {

    private val builder = AlertDialog.Builder(context, R.style.progressDialogTheme)
    private var dialog: AlertDialog
    init {
        val view = LayoutInflater.from(context).inflate(R.layout.my_progress_dialog, null)
        builder.setView(view)
        builder.setCancelable(false)
        dialog = builder.create()
    }

    fun show(){
        dialog.show()
    }
    fun hide(){
        dialog.dismiss()
    }
}