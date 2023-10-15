package com.deepdev.hamrochat.activities


import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
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

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(com.google.android.material.R.anim.abc_fade_in, com.google.android.material.R.anim.abc_fade_out)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        userId = firebaseAuth.currentUser!!.uid




        disablePostButton()


        onBackArrowClicked()


        binding.tvPostButton.setOnClickListener {

            progressDialog.show()
            uploadWithoutImage()
        }

        etPostTypingWatcher()
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

                } else {
                    enablePostButton()
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
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
}