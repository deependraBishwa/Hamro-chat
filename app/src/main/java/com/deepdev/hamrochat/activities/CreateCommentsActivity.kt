package com.deepdev.hamrochat.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.deepdev.hamrochat.R
import com.deepdev.hamrochat.adapters.CommentAdapter
import com.deepdev.hamrochat.databinding.ActivityCreateCommentsBinding
import com.deepdev.hamrochat.model.CommentModel
import com.deepdev.hamrochat.utils.MyProgressDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener


class CreateCommentsActivity : AppCompatActivity() {

    private val binding by lazy { ActivityCreateCommentsBinding.inflate(layoutInflater)}
    private val userId by lazy {  FirebaseAuth.getInstance().currentUser?.uid}
    private lateinit var commentsList : ArrayList<CommentModel>
    private lateinit var commentAdapter : CommentAdapter
    private val progressBar by lazy { MyProgressDialog(this) }

    private var postId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        progressBar.show()
        intent.let {
            postId = it.getStringExtra("postId").toString()
        }



        recyclerViewSetup()

        buttonSendDisabled()

        textWatcherForCommentBox()

        binding.btnSendComment.setOnClickListener {
            sendButtonClick()
        }

    }

    private fun fetchComments() {
        val commentRef = FirebaseDatabase.getInstance().getReference("comments").child(postId)
            commentRef.addValueEventListener(
                object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        commentsList.clear()
                        for (snap in snapshot.children){
                            val comment = snap.getValue(CommentModel::class.java)
                            commentsList.add(comment!!)
                        }
                        commentAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                    }

                }
            )
        progressBar.hide()
    }


    private fun recyclerViewSetup() {
        commentsList = ArrayList()
        commentAdapter = CommentAdapter(commentsList, this)
        val manager = LinearLayoutManager(this)
        binding.recyclerviewCreateComment.apply {
            adapter = commentAdapter
            layoutManager = manager

        }
        fetchComments()

    }


    private fun textWatcherForCommentBox() {
        binding.edtTextCommentBox.addTextChangedListener(commentWatcher)
    }

    private val commentWatcher= object : TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s.toString().isNotEmpty()){
                buttonSendEnabled()
            }else{
                buttonSendDisabled()
            }
        }

        override fun afterTextChanged(s: Editable?) {
        }

    }

    private fun buttonSendEnabled() {
        binding.btnSendComment.isEnabled = true
        binding.btnSendComment.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_green))
    }

    private fun buttonSendDisabled() {
        binding.btnSendComment.isEnabled = false
        binding.btnSendComment.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
    }

    private fun sendButtonClick() {
        val commentRef = FirebaseDatabase.getInstance().getReference("comments")
            .child(postId).push()

        val commentText = binding.edtTextCommentBox.text.toString().trim()
        val commentBy = userId
        val commentId = commentRef.key.toString()
        val timestamp = ServerValue.TIMESTAMP

        val commentMap = hashMapOf(
            "comment" to commentText,
            "commentId" to commentId,
            "commentBy" to commentBy,
            "date" to timestamp
        )
        binding.edtTextCommentBox.setText("")
        buttonSendDisabled()
        commentRef.setValue(commentMap)
    }

}