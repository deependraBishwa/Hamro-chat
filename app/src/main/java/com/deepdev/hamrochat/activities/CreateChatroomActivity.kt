package com.deepdev.hamrochat.activities

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import java.io.ByteArrayOutputStream
import java.io.InputStream

class CreateChatroomActivity : AppCompatActivity() {

    private val binding by lazy { ActivityCreateChatroomBinding.inflate(layoutInflater) }
    private val currentUser by lazy { FirebaseAuth.getInstance().currentUser?.uid.toString() }
    private val storage by lazy { FirebaseStorage.getInstance() }
    private val progressDialog by lazy { MyProgressDialog(this) }

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



    private fun compressImageToByteArray(bitmap: Bitmap, targetSizeKB: Int): ByteArray? {
        val maxQuality = 100 // Maximum quality (no compression)
        val stream = ByteArrayOutputStream()
        var quality = maxQuality

        // Compress the image to a byte array
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

        // Check if the compressed image is within the desired size
        while (stream.toByteArray().size / 1024 > targetSizeKB && quality > 0) {
            stream.reset() // Reset the stream for next compression
            quality -= 10
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        }

        return if (quality > 0) {
            stream.toByteArray()
        } else {
            null // Compression failed to reduce the size below the target
        }
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
    fun compressImageUriToByteArray(contentResolver: ContentResolver, imageUri: Uri): ByteArray? {
        try {
            // Get the InputStream from the image URI
            val inputStream: InputStream = contentResolver.openInputStream(imageUri) ?: return null

            // Decode the InputStream into a Bitmap
            val originalBitmap: Bitmap = BitmapFactory.decodeStream(inputStream)

            // Compress the bitmap to reduce memory size
            val compressedBitmap = compressBitmap(originalBitmap)

            // Convert the compressed bitmap to a byte array
            val compressedByteArray = convertBitmapToByteArray(compressedBitmap)

            // Close the InputStream
            inputStream.close()

            return compressedByteArray
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun compressBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val aspectRatio: Float = width.toFloat() / height.toFloat()

        val targetWidth = 800 // Adjust the target width as needed
        val targetHeight = (targetWidth / aspectRatio).toInt()

        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false)
    }

    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream) // Adjust quality as needed
        return byteArrayOutputStream.toByteArray()
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
                val uploadTask = imageRef.putBytes(compressImageUriToByteArray(contentResolver, imageUri!!)!!)

                uploadTask.addOnSuccessListener { _ ->

                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val db = FirebaseFirestore.getInstance()
                        val chatroomCollection = db.collection("chatrooms").document()
                        val chatroomId = chatroomCollection.id
                        val chatroomCreatedDate = FieldValue.serverTimestamp()

                        val chatroomData = hashMapOf(
                            "chatroomId" to chatroomId,
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
                                Toast.makeText(
                                    applicationContext, "Successfully created a new chatroom",
                                    Toast.LENGTH_LONG
                                ).show()
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

