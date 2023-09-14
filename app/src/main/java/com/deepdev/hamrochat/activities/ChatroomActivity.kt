package com.deepdev.hamrochat.activities

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.deepdev.hamrochat.R
import com.deepdev.hamrochat.adapters.ChatroomSmsAdapter
import com.deepdev.hamrochat.databinding.ActivityChatroomBinding
import com.deepdev.hamrochat.model.ChatroomSmsModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.UUID


class ChatroomActivity : AppCompatActivity() {
    private var chatroomId: String? = ""
    private var chatroomName: String? = ""
    private var username : String?=""
    private var userImageUrl : String?=""
    private var name : String?=""
    private var gender : String?=""

    private lateinit var messageList : ArrayList<ChatroomSmsModel>
    private lateinit var messageAdapter: ChatroomSmsAdapter

    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val currentUser by lazy { firebaseAuth.currentUser?.uid.toString() }
    private val firebaseDatabase by lazy { FirebaseFirestore.getInstance() }
    private val binding by lazy { ActivityChatroomBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        getUserDetails()
        disableButton()
        intent?.let {
            chatroomId = it.getStringExtra("chatroomId")
            chatroomName = it.getStringExtra("chatroomName")

        }

        setToolbar()

        binding.btnSend.setOnClickListener {
            sendMessage()
        }
        binding.edtMessageBox.addTextChangedListener(textWatcher)


        recyclerViewSetupForMessage()

    }

    private fun getUserDetails() {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")

        usersCollection.document(currentUser)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    username = documentSnapshot.getString("username") ?: ""
                    userImageUrl = documentSnapshot.getString("imageUrl") ?: ""
                    name = documentSnapshot.getString("name") ?: ""
                    gender = documentSnapshot.getString("gender") ?: ""
                } else {
                    // Handle the case where the user document does not exist
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occur during the fetch
            }
    }


    private fun setToolbar() {
        binding.toolbar.title = chatroomName
        setSupportActionBar(binding.toolbar)
    }

    private fun recyclerViewSetupForMessage() {
        messageList = ArrayList()
        messageAdapter = ChatroomSmsAdapter(messageList, applicationContext, currentUser)
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerChatroomActivity.adapter = messageAdapter
        binding.recyclerChatroomActivity.layoutManager = layoutManager

        fetchData()

    }

    private fun fetchData() {
        val db = FirebaseFirestore.getInstance()
        val messagesCollection = db.collection("messages").document(chatroomId!!).collection("chats")

        messagesCollection
            .orderBy("timestamp", Query.Direction.ASCENDING) // Order by timestamp in ascending order
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    // Handle the error
                    return@addSnapshotListener
                }

                messageList.clear()

                if (querySnapshot != null) {
                    for (document in querySnapshot.documents) {
                        val sms = document.toObject(ChatroomSmsModel::class.java)
                        sms?.let { messageList.add(it) }
                    }

                    messageAdapter.notifyDataSetChanged()
                }

                // Scroll to the last item in the RecyclerView
                binding.recyclerChatroomActivity.scrollToPosition(messageList.size - 1)
            }
    }


    private val textWatcher = object : TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s.toString().isNullOrEmpty() ){
                disableButton()
            }else{
                enableButton()
            }
        }

        override fun afterTextChanged(s: Editable?) {

        }

    }

    private fun disableButton() {
        binding.btnSend.isEnabled = false
        binding.btnSend.drawable?.setTint(Color.GRAY)
    }

    private fun enableButton() {
        binding.btnSend.isEnabled = true
        binding.btnSend.drawable?.setTint(ContextCompat.getColor(this, R.color.dark_green))
    }

    private fun sendMessage() {
        val db = FirebaseFirestore.getInstance()
        val messagesCollection = db.collection("messages").document(chatroomId!!).collection("chats")

        val text = binding.edtMessageBox.text.toString().trim()
        val timestamp = FieldValue.serverTimestamp()
        val authorUid = currentUser
        val messageId = UUID.randomUUID().toString() // Use a unique ID for each message

        val messageObj = hashMapOf(
            "message" to text,
            "timestamp" to timestamp,
            "authorUid" to authorUid,
            "messageId" to messageId,
            "authorUsername" to username,
            "authorImage" to userImageUrl,
        )

        messagesCollection
            .add(messageObj)
            .addOnSuccessListener { documentReference ->
                // The message was successfully added to Firestore
                binding.recyclerChatroomActivity.scrollToPosition(messageAdapter.itemCount - 1)
                binding.edtMessageBox.setText("")
            }
            .addOnFailureListener { exception ->
                // Handle the case where the message failed to be added
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            }
    }

}

