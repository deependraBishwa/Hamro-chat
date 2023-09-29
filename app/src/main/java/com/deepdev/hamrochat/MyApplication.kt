package com.deepdev.hamrochat

import android.app.Application
import com.deepdev.hamrochat.model.UserDataModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MyApplication : Application() {

    private val currentUser by lazy { FirebaseAuth.getInstance().currentUser?.uid.toString() }
    private lateinit var userDataModel: UserDataModel
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        fetchFromFirebase()
    }

    private fun fetchFromFirebase(){
        val userModel  : UserDataModel? = null

            Firebase.firestore.collection("users").document(currentUser)
                .get().addOnSuccessListener { document ->
                    val dob = document.getString("dateOfBirth") ?: ""
                    val gender = document.getString("gender")  ?: ""
                    val img = document.getString("imageUrl") ?: ""
                    val name = document.getString("name") ?: ""
                    val uid = document.getString("uid") ?: ""
                    val username = document.getString("username") ?: ""
                    userDataModel = UserDataModel(dob, gender, img, name, uid, username)

        }

    }
    fun getUserData() : UserDataModel{
       return userDataModel
    }
}