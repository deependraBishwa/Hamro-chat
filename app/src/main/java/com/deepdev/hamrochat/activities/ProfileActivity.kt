package com.deepdev.hamrochat.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.deepdev.hamrochat.MyApplication
import com.deepdev.hamrochat.R
import com.deepdev.hamrochat.databinding.ActivityProfileBinding
import com.deepdev.hamrochat.model.UserDataModel
import com.deepdev.hamrochat.utils.MyUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileActivity : AppCompatActivity() {

    private lateinit var app: MyApplication
    private lateinit var userData: UserDataModel
    private val binding by lazy { ActivityProfileBinding.inflate(layoutInflater) }
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val currentUser by lazy { firebaseAuth.currentUser?.uid.toString() }

    private lateinit var imageLauncher: ActivityResultLauncher<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        app = application as MyApplication
        userData = app.getUserData()

        initializeImageLauncher()
        binding.addImage.setOnClickListener{

            imageLauncher.launch("image/*")
        }

        binding.profileName.text = userData.name
        Glide.with(this).load(userData.imageUrl).placeholder(R.drawable.user_place_holder)
            .into(binding.profileImage)

    }

    private fun initializeImageLauncher() {

        imageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // Handle the selected image URI here
            if (uri != null) {
                // Do something with the selected image URI, like displaying it in an ImageView
                uploadImage(uri )
                binding.profileImage.setImageURI(uri)

            }
        }
    }
    private fun uploadImage(imageUri : Uri) {
        CoroutineScope(Dispatchers.IO).launch{
            val imageRef = FirebaseStorage.getInstance().getReference("images").child(currentUser)
                .child(imageUri?.lastPathSegment!!)

            val uploadTask = imageRef.putFile(imageUri!!)
            uploadTask.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                   FirebaseStorage.getInstance().getReferenceFromUrl(userData.imageUrl).delete()
                       .addOnSuccessListener {
                       Firebase.firestore.collection("users").document(currentUser)
                           .update("imageUrl", uri.toString()).addOnSuccessListener {
                               MyUtils.showToast(applicationContext, "success")
                               userData.imageUrl = uri.toString()
                           }.addOnFailureListener {
                               MyUtils.showToast(applicationContext, "success")

                           }
                   }
                }
            }.await()
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_sign_out -> {
                firebaseAuth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
            }
            android.R.id.home -> {
                finish()
            }
        }
        return true
    }
}