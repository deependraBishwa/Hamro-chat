package com.deepdev.hamrochat.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.deepdev.hamrochat.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private val binding by lazy { ActivityRegisterBinding.inflate(layoutInflater) }
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val currentUser by lazy { firebaseAuth.currentUser?.uid }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private  var  email=""
    private  var  pass= ""
    private  var  rePass=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)



        onRegisterButtonClick()

        onAlreadyUserClick()
    }

    private fun onAlreadyUserClick() {
        binding.tvAlreadyUser.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))

        }
    }

    private fun onRegisterButtonClick() {
        binding.btnRegister.setOnClickListener{
            val email = binding.edtEmail.text.toString().trim()
            val pass = binding.edtPass.text.toString().trim()
            val rePass = binding.edtPassRe.text.toString().trim()

            if (email.isEmpty()){
                binding.edtEmail.error="please fill"
                return@setOnClickListener
            }
            if (pass.isEmpty()){
                binding.edtPass.error="please fill"
                return@setOnClickListener

            }
            if (rePass.isEmpty()){
                binding.edtPassRe.error="please fill"
                return@setOnClickListener

            }
            if (isPassAndRePassSame(pass, rePass)){
                firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                    task ->
                    if (task.isSuccessful){
                        val intent = Intent(this, GetUserDetail::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else{
                        Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT ).show()

                    }

                }
            }else{
                binding.edtPassRe.error = "password does not match"
            }
        }
    }

    private fun isPassAndRePassSame(p: String, rP : String): Boolean {
        if (p == rP){
            return true
        }
        return false
    }
}