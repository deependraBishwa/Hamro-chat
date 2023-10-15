package com.deepdev.hamrochat.activities

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.AbsListView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.deepdev.hamrochat.R
import com.deepdev.hamrochat.adapters.ChatroomSmsAdapter
import com.deepdev.hamrochat.adapters.ChatroomUserAdapter
import com.deepdev.hamrochat.databinding.ActivityChatroomBinding
import com.deepdev.hamrochat.model.ChatroomSmsModel
import com.deepdev.hamrochat.model.User
import com.deepdev.hamrochat.utils.MyBottomSheetDialog
import com.deepdev.hamrochat.utils.MyUtils
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ChatroomActivity : AppCompatActivity() {
    private var isScrolling = false
    private var isLoading = false
    private var isLastPage = false
    private var lastTimeStamp: Timestamp? = null
    private lateinit var enteredTime: FieldValue
    private var chatroomId: String? = ""
    private var chatroomName: String? = ""

    // message properties
    private var messageList = ArrayList<ChatroomSmsModel>()
    private lateinit var messageAdapter: ChatroomSmsAdapter

    // user who are chatting in the chatroom properties
    private var userList = ArrayList<User>()
    private lateinit var userDataAdapter: ChatroomUserAdapter
    private val firestore by lazy { FirebaseFirestore.getInstance() }


    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val currentUserUid by lazy { firebaseAuth.currentUser?.uid.toString() }
    private val binding by lazy { ActivityChatroomBinding.inflate(layoutInflater) }


    private var currentUser: User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        // initially send button is disabled
        // until it has text
        disableButton()

        // chatroom id through intent
        intent?.let {
            chatroomId = it.getStringExtra("chatroomId")
            chatroomName = it.getStringExtra("chatroomName")
            // get entered users information
            // and save it to chatroom database
            getCurrentUsersDetails()

            // fetch room info as well
            getRoomInfo()

            // fetch the list of the users
            // who are current in chatroom
        }



        setToolbar()
        recyclerViewSetupForMessage()

        listenRealtime()


        binding.btnSend.setOnClickListener {

            sendMessage()
        }

        binding.edtMessageBox.addTextChangedListener(textWatcher)


        //   recyclerViewScrollListener()
    }


    private fun getCurrentUsersDetails() {
        firestore.collection("users")
            .document(currentUserUid).get().addOnSuccessListener { documentRef ->
                documentRef.let {
                    currentUser = it.toObject(User::class.java)
                    currentUser?.let {

                        // when user enter in chatroom save
                        // user detail to chatroom database
                        userEnterInChatroom(it)
                    }
                }
            }

    }

    private fun getRoomInfo() {
        Firebase.firestore.collection("chatrooms").document(chatroomId!!)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    MyUtils.showToast(this, error.message.toString())
                    return@addSnapshotListener
                }
                if (value != null && value.exists()) {

                }
            }
    }



    private fun userEnterInChatroom(currentUser: User) {
        CoroutineScope(Dispatchers.IO).launch {

            enteredTime = FieldValue.serverTimestamp()

            val userdata = mutableMapOf(
                "dateOfBirth" to currentUser.dateOfBirth,
                "gender" to currentUser.gender,
                "imageUrl" to currentUser.imageUrl,
                "join_date" to enteredTime,
                "name" to currentUser.name,
                "username" to currentUser.username,
                "uid" to currentUserUid
            )
            val userInChatRef = Firebase.firestore.collection("user_live_in_chatroom")
                .document(chatroomId!!).collection("users").document(currentUserUid)
            userInChatRef.set(userdata.toMap())


        }
    }


    private fun setToolbar() {
        binding.toolbar.title = chatroomName
        setSupportActionBar(binding.toolbar)

        // change the color of the 3 dot menu
        val overflowIcon = binding.toolbar.overflowIcon
        overflowIcon?.setTint(ContextCompat.getColor(this, R.color.white))
    }



    private fun recyclerViewSetupForMessage() {
        messageAdapter = ChatroomSmsAdapter(messageList, applicationContext, currentUserUid)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        binding.recyclerChatroomActivity.adapter = messageAdapter
        binding.recyclerChatroomActivity.layoutManager = layoutManager



        binding.recyclerChatroomActivity.addOnScrollListener(object : OnScrollListener(){

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    isScrolling = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val scrolledItem = layoutManager.findFirstVisibleItemPosition()
                val totalItem = layoutManager.itemCount
                val visibleItem = layoutManager.childCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                Log.d("gunda", "scrolled: $scrolledItem")
                Log.d("gunda", "item: $totalItem")
                Log.d("gunda", "visible: $lastVisibleItem")
                Log.d("gunda", "visibleItem: $visibleItem")

                if (isScrolling && !isLoading && !isLastPage &&
                    totalItem <= (scrolledItem+lastVisibleItem)){
                    isLoading = true
                    MyUtils.showToast(applicationContext, "Loading..")
                    getPreviousMessages()
                }
            }
        })

    }

    private fun getPreviousMessages(){

        firestore.collection("messages")
            .document(chatroomId!!)
            .collection("chats")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .startAfter(lastTimeStamp)
            .limit(15)
            .get().addOnSuccessListener {
                documentRef ->
                isLoading = false
                if (documentRef.isEmpty){
                    isLastPage = true
                    MyUtils.showToast(applicationContext, "no more data found")
                    return@addOnSuccessListener
                }

                if(lastTimeStamp != null){
                    lastTimeStamp = documentRef.documents[documentRef.documents.size - 1].getTimestamp("timestamp")
                }
                documentRef?.let {

                    for (sms in documentRef.documents){
                        val message = sms.toObject(ChatroomSmsModel::class.java)
                        message?.let {
                            messageList.add(it)
                        }
                    }
                    messageAdapter.notifyDataSetChanged()
                }
            }
    }
    private fun listenRealtime() {
        if (chatroomId == null) return

        val query = firestore.collection("messages")
            .document(chatroomId!!)
            .collection("chats")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(15)


        query.addSnapshotListener { querySnapshot, error ->

            if (error != null) {
                return@addSnapshotListener
            }

            if (querySnapshot != null && !querySnapshot.isEmpty) {
                isLastPage = false
                    lastTimeStamp = querySnapshot.documents[querySnapshot.documents.size -1].getTimestamp("timestamp")
                messageList.clear()
                for (sms in querySnapshot.documents) {
                    sms.toObject(ChatroomSmsModel::class.java)
                        ?.let { it1 ->
                            messageList.add(it1)
                            Log.d("hello", "listenRealtime: ${it1.message}")

                        }
                }
                messageAdapter.notifyDataSetChanged()
            }
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
        binding.btnSend.drawable?.setTint(ContextCompat.getColor(this, R.color.main_accent_color))
    }

    private fun sendMessage() {

        val db = FirebaseFirestore.getInstance()
        val messagesCollection = db.collection("messages")
            .document(chatroomId!!).collection("chats")
        val messageId = messagesCollection.id

        val text = binding.edtMessageBox.text.toString().trim()
        val timestamp = FieldValue.serverTimestamp()
        val authorUid = currentUserUid

        binding.edtMessageBox.setText("")
        val messageObj = hashMapOf(
            "message" to text,
            "timestamp" to timestamp,
            "authorUid" to authorUid,
            "messageId" to messageId
        )

        messagesCollection
            .add(messageObj)
            .addOnSuccessListener { _ ->
                // The message was successfully added to Firestore
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
            .collection("users").document(currentUserUid)

        userInChatRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                userInChatRef.delete()
            }
        }
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.menu_users -> {
                showBottomLinear()
            }
        }
        return true
    }

    private fun showBottomLinear() {
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_chat_activity, null)

        val btnClose = view.findViewById<ImageView>(R.id.image_close)

        val bottomSheet = MyBottomSheetDialog(chatroomId!!)
        bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        btnClose.setOnClickListener { bottomSheet.dismiss() }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_chatroom_act, menu)
        return super.onCreateOptionsMenu(menu)
    }

}

