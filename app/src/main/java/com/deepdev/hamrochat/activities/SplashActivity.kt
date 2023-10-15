package com.deepdev.hamrochat.activities

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class SplashActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkInternetConnection()
        firebaseAuth = FirebaseAuth.getInstance()

        val currentUser = firebaseAuth.currentUser

        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            val db = FirebaseFirestore.getInstance()
            val usersCollection = db.collection("users")

            usersCollection.document(currentUser.uid).get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // User data exists in Firestore
                    Toast.makeText(applicationContext, "Successfully logged in", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(applicationContext, Main2Activity::class.java))
                    finishAffinity()
                } else {
                    // User data does not exist in Firestore
                    val intent = Intent(applicationContext, GetUserDetail::class.java)
                    startActivity(intent)
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(applicationContext, exception.message, Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun checkInternetConnection() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

        val isInternetConnected = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        val isConnectionFast = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true

        if (isInternetConnected && isConnectionFast) {
            // Internet is connected and the connection is fast
            Toast.makeText(this, "Internet is connected and speed is good", Toast.LENGTH_SHORT).show()
        } else {
            // Internet is not connected or the connection is slow
            Toast.makeText(this, "No or slow internet connection", Toast.LENGTH_SHORT).show()
            return
        }
    }
}