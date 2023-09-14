package com.deepdev.hamrochat.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.deepdev.hamrochat.databinding.ActivityLoginBinding
import com.deepdev.hamrochat.utils.MyProgressDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class LoginActivity : AppCompatActivity() {

    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val currentUser by lazy { firebaseAuth.currentUser?.uid.toString() }
    private val progressDialog by lazy { MyProgressDialog(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {

            hideKeyBoard()

            val email = binding.edtEmail.text.toString().trim()
            val pass = binding.edtPass.text.toString().trim()

            if (email.isEmpty()){
                binding.edtEmail.error="required"
                return@setOnClickListener
            }
            if (pass.isEmpty()){
                binding.edtPass.error="required"
                return@setOnClickListener
            }
            progressDialog.show()
            firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = firebaseAuth.currentUser // Get the currently signed-in user

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
                                    progressDialog.hide()
                                    finishAffinity()
                                } else {
                                    // User data does not exist in Firestore
                                    val intent = Intent(applicationContext, GetUserDetail::class.java)
                                    startActivity(intent)
                                }
                            }
                            .addOnFailureListener { exception ->
                                progressDialog.hide()
                                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    progressDialog.hide()
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }

        }

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }



    }

    private fun hideKeyBoard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.edtPass.windowToken, 0)
    }
}