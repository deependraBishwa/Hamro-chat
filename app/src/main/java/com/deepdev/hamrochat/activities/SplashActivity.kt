package com.deepdev.hamrochat.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class SplashActivity : AppCompatActivity() {

    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val currentUser by lazy { firebaseAuth.currentUser!! }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        if (currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            val usersCollection = db.collection("users")

            usersCollection.document(currentUser.uid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
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
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(applicationContext, exception.message, Toast.LENGTH_SHORT).show()
                }
        } else {
            // User is not signed in, navigate to the login activity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }


    }
}