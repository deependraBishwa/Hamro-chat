package com.deepdev.hamrochat.activities

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.deepdev.hamrochat.MyApplication
import com.deepdev.hamrochat.R
import com.deepdev.hamrochat.adapters.ChatroomSmsAdapter
import com.deepdev.hamrochat.databinding.ActivityChatroomBinding
import com.deepdev.hamrochat.model.ChatroomSmsModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.UUID


class ChatroomActivity : AppCompatActivity() {
    private var chatroomId: String? = ""
    private var chatroomName: String? = ""

    private lateinit var messageList: ArrayList<ChatroomSmsModel>
    private lateinit var messageAdapter: ChatroomSmsAdapter

    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val currentUser by lazy { firebaseAuth.currentUser?.uid.toString() }
    private val binding by lazy { ActivityChatroomBinding.inflate(layoutInflater) }
    private val app by lazy {application as  MyApplication }
    private val userDataModel by lazy { app.getUserData() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        disableButton()
        intent?.let {
            chatroomId = it.getStringExtra("chatroomId")
            chatroomName = it.getStringExtra("chatroomName")
            userEnterInChatroom()
        }


        setToolbar()

        binding.btnSend.setOnClickListener {
            sendMessage()
        }
        binding.edtMessageBox.addTextChangedListener(textWatcher)


        recyclerViewSetupForMessage()

    }

    private fun userEnterInChatroom() {
        val emptyData = mutableMapOf<String, Any>()
        val userInChatRef = Firebase.firestore.collection("user_live_in_chatroom")
            .document(chatroomId!!).collection("users").document(currentUser)
        userInChatRef.set(emptyData)

    }




    private fun setToolbar() {
        binding.toolbar.title = chatroomName
        setSupportActionBar(binding.toolbar)

        // change the color of the 3 dot menu
        val overflowIcon = binding.toolbar.overflowIcon
        overflowIcon?.setTint(ContextCompat.getColor(this,  R.color.white))
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


    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s.toString().isNullOrEmpty()) {
                disableButton()
            } else {
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
        val messagesCollection = db.collection("messages")
            .document(chatroomId!!).collection("chats")

        val text = binding.edtMessageBox.text.toString().trim()
        val timestamp = FieldValue.serverTimestamp()
        val authorUid = currentUser
        val messageId = UUID.randomUUID().toString() // Use a unique ID for each message

        val messageObj = hashMapOf(
            "message" to text,
            "timestamp" to timestamp,
            "authorUid" to authorUid,
            "messageId" to messageId,
            "authorUsername" to userDataModel.username,
            "authorImage" to userDataModel.imageUrl,
        )

        messagesCollection
            .add(messageObj)
            .addOnSuccessListener { _ ->
                // The message was successfully added to Firestore
                binding.recyclerChatroomActivity.scrollToPosition(messageAdapter.itemCount - 1)
                binding.edtMessageBox.setText("")
            }
            .addOnFailureListener { exception ->
                // Handle the case where the message failed to be added
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            }
    }


    // delete the user from chatroom when lives the chatroom
    override fun onDestroy() {
        val userInChatRef = Firebase.firestore.collection("user_live_in_chatroom")
            .document(chatroomId!!)
            .collection("users").document(currentUser)

        userInChatRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                userInChatRef.delete()
            }
        }
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home-> finish()
            R.id.menu_users-> Toast.makeText(applicationContext, "users", Toast.LENGTH_SHORT).show()
        }
        return true
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_chatroom_act, menu)
        return super.onCreateOptionsMenu(menu)
    }
}

