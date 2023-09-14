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

            val storageRef = FirebaseStorage.getInstance().reference.child("images").child(currentUser)
            val imageRef = storageRef.child(imageUri!!.lastPathSegment!!)
            val uploadTask = imageRef.putFile(imageUri!!)

            uploadTask.addOnSuccessListener { uploadTaskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val db = FirebaseFirestore.getInstance()
                    val chatroomsCollection = db.collection("chatrooms")

                    val chatroomName = "Your Chatroom Name" // Replace with the actual chatroom name
                    val country = "Your Country" // Replace with the actual country
                    val welcomeMessage = "Welcome to the chatroom!" // Replace with the actual welcome message
                    val chatroomCreatedDate = FieldValue.serverTimestamp()

                    val chatroomData = hashMapOf(
                        "chatroomId" to db.collection("chatrooms").document().id,
                        "chatroomImage" to uri.toString(),
                        "chatroomName" to chatroomName,
                        "country" to country,
                        "welcomeMessage" to welcomeMessage,
                        "chatroomCreatedDate" to chatroomCreatedDate,
                        "adminUid" to currentUser
                    )

                    chatroomsCollection.add(chatroomData)
                        .addOnSuccessListener { documentReference ->
                            progressDialog.hide()
                            Toast.makeText(this, "Success", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener { exception ->
                            progressDialog.hide()
                            Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
                        }
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }

        }
    }


}

private fun getCurrentMillis(): String {
    return System.currentTimeMillis().toString()
}
