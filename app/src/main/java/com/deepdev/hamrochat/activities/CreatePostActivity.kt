package com.deepdev.hamrochat.activities


import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.deepdev.hamrochat.R
import com.deepdev.hamrochat.databinding.ActivityCreatePostBinding
import com.deepdev.hamrochat.utils.MyProgressDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class CreatePostActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private val currentUser by lazy { firebaseAuth.currentUser?.uid.toString() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private lateinit var userId: String
    private val progressDialog by lazy { MyProgressDialog(this) }

    private val binding by lazy { ActivityCreatePostBinding.inflate(layoutInflater) }
    private var imageLauncher: ActivityResultLauncher<String>? = null
    private var imageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(com.google.android.material.R.anim.abc_fade_in, com.google.android.material.R.anim.abc_fade_out)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        userId = firebaseAuth.currentUser!!.uid




        disablePostButton()

        initializeImageLauncher()

        onBackArrowClicked()

        onAddImageClicked()

        binding.tvPostButton.setOnClickListener {

            progressDialog.show()
            if (imageUri == null){
                uploadWithoutImage()
            }
            if (imageUri !=null){
                prepareUploadWithImage()
            }
        }

        etPostTypingWatcher()

    }

    private fun uploadImage() {
        CoroutineScope(Dispatchers.IO).launch{
            val imageRef = FirebaseStorage.getInstance().getReference("images").child(currentUser)
                .child(imageUri?.lastPathSegment!!)

            val uploadTask = imageRef.putFile(imageUri!!)
            uploadTask.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    uploadPostWithImage(uri.toString())
                }
            }.await()
        }
    }

    private fun uploadPostWithImage(url: String) {
        CoroutineScope(Dispatchers.IO).launch{
            try {
                Firebase.firestore.runBatch { batch ->
                    val newPostRef = firestore.collection("posts").document()
                    val postId = newPostRef.id
                    val text = binding.etWhatsOnYourMind.text.toString()
                    val timestamp = FieldValue.serverTimestamp()
                    val authorId = currentUser

                    val data = mutableMapOf(
                        "postId" to postId,
                        "text" to text,
                        "timestamp" to timestamp,
                        "authorId" to authorId,
                        "image" to url
                    )

                    // Use set instead of update to create a new document
                    batch.set(newPostRef, data)
                    progressDialog.hide()
                }.await()
            }catch (e: Exception ){
                withContext(Dispatchers.Main){
                    progressDialog.hide()
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }

    private fun prepareUploadWithImage() {

        uploadImage()

    }

    private fun uploadWithoutImage() {

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Firebase.firestore.runBatch { batch ->
                    val newPostRef = firestore.collection("posts").document()
                    val postId = newPostRef.id
                    val text = binding.etWhatsOnYourMind.text.toString()
                    val timestamp = FieldValue.serverTimestamp()
                    val authorId = currentUser
                    val image = ""

                    val data = mutableMapOf(
                        "postId" to postId,
                        "text" to text,
                        "timestamp" to timestamp,
                        "authorId" to authorId,
                        "image" to image
                    )

                    // Use set instead of update to create a new document
                    batch.set(newPostRef, data)
                }.await()

                // Batch write completed successfully
                withContext(Dispatchers.Main) {
                    progressDialog.hide()
                    Toast.makeText(applicationContext, "Successfully uploaded", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Handle any exceptions that occurred during the batch write
                withContext(Dispatchers.Main) {
                    progressDialog.hide()
                    Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }



    private fun etPostTypingWatcher() {
        binding.etWhatsOnYourMind.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (s.toString().isEmpty()) {
                    if (imageUri == null) {
                        disablePostButton()
                    } else {
                        enablePostButton()
                    }
                } else {
                    if (imageUri != null) {
                    }
                    enablePostButton()
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    private fun initializeImageLauncher() {

        imageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // Handle the selected image URI here
            if (uri != null) {
                // Do something with the selected image URI, like displaying it in an ImageView
                imageUri = uri
                binding.etWhatsOnYourMind.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                binding.etWhatsOnYourMind.requestLayout()
                binding.ivPostImage.visibility = View.VISIBLE
                binding.ivPostImage.setImageURI(imageUri)
                enablePostButton()
            } else {
                disablePostButton()
            }
        }
    }

    private fun enablePostButton() {
        binding.tvPostButton.let {
            it.isEnabled = true
            it.background = ContextCompat.getDrawable(this, R.drawable.ripple_text_view)
            it.setTextColor(Color.parseColor("#ffffff"))
        }
    }

    private fun disablePostButton() {
        binding.tvPostButton.let {
            it.isEnabled = false
            it.background = ContextCompat.getDrawable(this, R.drawable.disable_btn_background)
            it.setTextColor(Color.parseColor("#f3f6f9"))
        }

    }


    private fun onBackArrowClicked() {
        binding.ivArrowBack.setOnClickListener {
            Utils.showDialogDiscard(this)
        }
    }

    object Utils {
        fun showDialogDiscard(context: Context) {

            val builder = AlertDialog.Builder(context)
            builder.setTitle("this is title")
            builder.setMessage("you wanna discard this post")
            builder.setPositiveButton("yes") { dialog, which ->
                (context as Activity).finish()
            }
            builder.setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            val dialog = builder.create()

            dialog.create()
            dialog.show()
        }
    }


    private fun onAddImageClicked() {
        binding.btnAddImage.setOnClickListener {
            imageLauncher!!.launch("image/*")
        }
    }
}