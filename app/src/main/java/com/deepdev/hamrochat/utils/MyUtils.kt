package com.deepdev.hamrochat.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import java.io.ByteArrayInputStream

object MyUtils {
    fun showToast(context: Context, msg: String){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
    fun base64StringToBitmap(base64String: String): Bitmap? {
        try {
            // Decode the Base64 string into a byte array
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)

            // Create a ByteArrayInputStream from the byte array
            val inputStream = ByteArrayInputStream(decodedBytes)

            // Create a Bitmap from the ByteArrayInputStream
            return BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}