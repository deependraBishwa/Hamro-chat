package com.deepdev.hamrochat.activities

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.deepdev.hamrochat.databinding.ActivityCreateChatroomBinding
import com.deepdev.hamrochat.utils.MyProgressDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CreateChatroomActivity : AppCompatActivity() {

    private val binding by lazy { ActivityCreateChatroomBinding.inflate(layoutInflater) }
    private val currentUser by lazy { FirebaseAuth.getInstance().currentUser?.uid.toString() }
    private val storage by lazy { FirebaseStorage.getInstance() }
    private val progressDialog by lazy {MyProgressDialog(this) }
    private var imageUri: Uri? = null
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initializeImagePicker()

        binding.addImage.setOnClickListener {
            selectImageFromGallery()
        }



        onCreateButtonClick()


    }

    private fun initializeImagePicker() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            // Handle the selected image URI here (uri is the image URI)
            if (uri != null) {
                binding.circleImage.setImageURI(uri)
                imageUri = uri
            }
        }
    }

    private fun selectImageFromGallery() {

        imagePickerLauncher.launch("image/*")
    }


    private fun onCreateButtonClick() {

        binding.btnCreate.setOnClickListener {

            val chatroomName = binding.edtChatRoomName.text.toString().trim()
            val country = binding.edtCountry.text.toString().trim()
            val welcomeMessage = binding.edtWelcomeMessage.text.toString().trim()
            if (imageUri == null) {
                Toast.makeText(this, "please select an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (chatroomName.isEmpty()) {
                binding.edtChatRoomName.error = "can not be empty"
                return@setOnClickListener
            }
            if (country.isEmpty()) {
                binding.edtCountry.error = "can not be empty"
                return@setOnClickListener
            }
            if (welcomeMessage.isEmpty()) {
                binding.edtWelcomeMessage.error = "can not be empty"
                return@setOnClickListener
            }

           progressDialog.show()

            CoroutineScope(Dispatchers.IO).launch {
                val storageRef = FirebaseStorage.getInstance().reference.child("images").child(currentUser)
                val imageRef = storageRef.child(FieldValue.serverTimestamp().toString())
                val uploadTask = imageRef.putFile(imageUri!!)

                uploadTask.addOnSuccessListener { uploadTaskSnapshot ->
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val db = FirebaseFirestore.getInstance()
                        val chatroomCollection = db.collection("chatrooms").document()
                        val chatroomId = chatroomCollection.id
                        val chatroomCreatedDate = FieldValue.serverTimestamp()

                        val chatroomData = hashMapOf(
                            "chatroomId" to chatroomId ,
                            "chatroomImage" to uri.toString(),
                            "chatroomName" to chatroomName,
                            "country" to country,
                            "welcomeMessage" to welcomeMessage,
                            "chatroomCreatedDate" to chatroomCreatedDate,
                            "adminUid" to currentUser
                        )

                        chatroomCollection.set(chatroomData)
                            .addOnSuccessListener { _ ->
                                progressDialog.hide()
                                Toast.makeText(applicationContext, "Successfully created a new chatroom",
                                    Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener { exception ->
                                progressDialog.hide()
                                Toast.makeText(applicationContext, exception.message, Toast.LENGTH_LONG).show()
                            }
                    }
                }.addOnFailureListener { e ->
                    progressDialog.hide()
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                }.await()

            }

        }
    }


}

private fun getCurrentMillis(): String {
    return System.currentTimeMillis().toString()
}
