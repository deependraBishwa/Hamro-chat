package com.deepdev.hamrochat.utils

import android.content.Context
import android.widget.Toast

object MyUtils {
    fun showToast(context: Context, msg: String){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
    fun getUserDetails(context: Context){

    }
}