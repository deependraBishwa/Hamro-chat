package com.deepdev.hamrochat.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.Button
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.deepdev.hamrochat.R
import com.deepdev.hamrochat.databinding.ActivityGetUserDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class GetUserDetail : AppCompatActivity() {

    private val binding by lazy { ActivityGetUserDetailBinding.inflate(layoutInflater) }
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val currentUser by lazy { firebaseAuth.currentUser?.uid.toString() }
    private var gender = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        datePickerOnTouchOfEditText()

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.radio_male -> {gender = "male"}
                R.id.radio_female -> {gender = "female"}
                R.id.radio_other -> {gender = "other"}
                else ->{
                }
            }
        }

        submitButtonClick()


    }

    private fun submitButtonClick() {
        binding.btnSubmit.setOnClickListener {
            val name = binding.edtName.text.toString()
            val username = binding.edtUsername.text.toString()
            val dateOfBirth = binding.edtDateOfBirth.text.toString()

            if (username.isEmpty()){
                binding.edtUsername.error = "can not be empty"
                return@setOnClickListener
            }
            if (name.isEmpty()){
                binding.edtName.error = "can not be empty"
                return@setOnClickListener
            }
            if (dateOfBirth.isEmpty()){
                binding.edtDateOfBirth.error = "can not be empty"
                return@setOnClickListener
            }
            if (gender == ""){
                Toast.makeText(this, "please choose your gender", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userMap = hashMapOf<String, Any>(
                "uid" to currentUser,
                "name" to name,
                "username" to username,
                "dateOfBirth" to dateOfBirth,
                "gender" to gender,
                "imageUrl" to ""
            )


            FirebaseDatabase.getInstance().getReference("usernames")
                .child(username).setValue(true)

            FirebaseFirestore.getInstance().collection("users")
                .document(currentUser).set(userMap).addOnCompleteListener {
                    if (it.isSuccessful){
                        val intent = Intent(applicationContext, Main2Activity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    }else{
                        return@addOnCompleteListener
                    }
                }
        }


    }

    private fun datePickerOnTouchOfEditText() {
        binding.edtDateOfBirth.setOnTouchListener { _, event ->
            // Handle touch events here
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {


                    true // Return true to consume the event
                }
                MotionEvent.ACTION_UP -> {
                    showDialogForDatePicker()
                    true // Return true to consume the event
                }
                else -> false
            }
        }
    }

    private fun showDialogForDatePicker() {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_date_picker, null)
        builder.setView(view)
        val dialog = builder.create()
        val datePicker = view.findViewById<DatePicker>(R.id.date_picker)
        val okay = view.findViewById<Button>(R.id.btn_okay)

        okay.setOnClickListener {
            binding.edtDateOfBirth.setText("${datePicker.dayOfMonth}/${datePicker.month}/${datePicker.year}")
            dialog.dismiss()
        }


        dialog.create()
        dialog.show()
    }
}