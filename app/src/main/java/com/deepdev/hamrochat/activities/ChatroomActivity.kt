package com.deepdev.hamrochat.activities

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.AbsListView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
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
import com.deepdev.hamrochat.utils.MyUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout


class ChatroomActivity() : AppCompatActivity() {
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setToolbar()
        recyclerViewSetupForMessage()
        // initially send button is disabled
        // until it has text
        disableButton()
        // chatroom id through intent
        intent?.let {
            chatroomId = it.getStringExtra("chatroomId")
            chatroomName = it.getStringExtra("chatroomName")
            // get entered users information
            // and save it to chatroom database

        }


        // save data related to chatroom and users
        // asynchronously
        saveAndFetchInformation()


        // when database is updates
        // fetch data
        listenRealtime()

        //when send button is clicked
        whenSendButtonClicked()

        // update send button according to
        // edittext
        editTextTextWatcher()

        // handle onBack press
        handleBackPressed()

    }

    private fun saveAndFetchInformation() {
        runBlocking {
            withTimeout(10000){
                try {
                    async { getUsersDetail(currentUserUid) }
                    async { getRoomInfo() }.await()
                    async { addToCurrentUsersRecentVisitedChatroomList() }.await()
                }catch (e : TimeoutCancellationException)
                {
                    MyUtils.showToast(applicationContext, e.message.toString())
                    e.printStackTrace()
                    finish()
                }

            }
        }
    }

    private fun whenSendButtonClicked() {
        binding.btnSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun editTextTextWatcher() {
        binding.edtMessageBox.addTextChangedListener(textWatcher)
    }

    private fun handleBackPressed() {
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            showAlertDialog()
        }

    }

    private fun showAlertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Exit chatroom ?")
        builder.setMessage("if you leave the room you can not see older message again.")
        builder.setPositiveButton(
            "Yes"
        ) { dialog, _ ->
            dialog.dismiss()
            finish()
            onBackPressedCallback.remove()
        }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.show()
    }

    private fun addToCurrentUsersRecentVisitedChatroomList() {
                val hashMap = hashMapOf(
                    "chatroomId" to chatroomId,
                    "joined_time" to FieldValue.serverTimestamp()
                )
                val recentJoinedChatroomRef = Firebase.firestore.collection("users")
                    .document(currentUserUid)
                    .collection("recent_chatroom").document(chatroomId!!)

                recentJoinedChatroomRef.get().addOnSuccessListener { documentRef ->
                    if (!documentRef.exists()) {
                        recentJoinedChatroomRef.set(hashMap)
                            .addOnSuccessListener {

                               binding.recyclerChatroomActivity.visibility = VISIBLE
                            }
                    } else {
                        recentJoinedChatroomRef.update(hashMap)
                            .addOnSuccessListener {
                               binding.recyclerChatroomActivity.visibility = VISIBLE

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


    private fun userEnterInChatroom(userDetail : User) {


                enteredTime = FieldValue.serverTimestamp()

                val userdata = mutableMapOf(
                    "dateOfBirth" to userDetail.dateOfBirth,
                    "gender" to userDetail.gender,
                    "imageUrl" to userDetail.imageUrl,
                    "name" to userDetail.name,
                    "username" to userDetail.username,
                    "uid" to userDetail.uid,
                    "join_time_stamp" to enteredTime
                )
                val userInChatRef = Firebase.firestore.collection("chatrooms")
                    .document(chatroomId!!).collection("current_users").document(currentUserUid)
                userInChatRef.set(userdata.toMap()).addOnSuccessListener {
                    Log.d("taskfinish", "onCreate: enter")

                }.addOnFailureListener {
                    MyUtils.showToast(applicationContext, it.message.toString())
                    userInChatRef.delete()
                }

    }

    private fun getUsersDetail(currentUserId: String) {

        Firebase.firestore.collection("users")
            .document(currentUserId)
            .get().addOnSuccessListener { documentRef ->

                val name = documentRef.getString("name") ?: ""
                val dob = documentRef.getString("dateOfBirth") ?: ""
                val gender = documentRef.getString("gender") ?: ""
                val imageUrl = documentRef.getString("imageUrl") ?: ""
                val uid = documentRef.getString("uid") ?: ""
                val username = documentRef.getString("username") ?: ""

                val user = User(dob, gender, imageUrl, name, uid, username)

                Log.d("chatroomid", "getUsersDetail: $chatroomId")
                userEnterInChatroom(user)

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



        binding.recyclerChatroomActivity.addOnScrollListener(object : OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val scrolledItem = layoutManager.findFirstVisibleItemPosition()
                val totalItem = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()


                //   Log.d("checkks", "onScrolled $totalItem = $scrolledItem  + $lastVisibleItem))}")
                if (isScrolling && !isLoading && !isLastPage &&
                    totalItem <= (scrolledItem + lastVisibleItem)
                ) {
                    isLoading = true
                    MyUtils.showToast(applicationContext, "Loading..")
                    getPreviousMessages()
                }
            }
        })

    }

    private fun getPreviousMessages() {

        binding.progressBar.visibility = View.VISIBLE
        firestore.collection("chatrooms")
            .document(chatroomId!!)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .startAfter(lastTimeStamp)
            .limit(15)
            .get().addOnSuccessListener { documentRef ->
                isLoading = false
                if (documentRef.isEmpty) {
                    isLastPage = true
                    binding.progressBar.visibility = View.GONE
                    MyUtils.showToast(applicationContext, "no more data found")
                    return@addOnSuccessListener
                }

                if (lastTimeStamp != null) {
                    lastTimeStamp = documentRef.documents[documentRef.documents.size - 1].getTimestamp("timestamp")
                }
                documentRef?.let {

                    for (sms in documentRef.documents) {
                        val message = sms.toObject(ChatroomSmsModel::class.java)
                        message?.let {
                            messageList.add(it)
                        }
                    }

                    binding.progressBar.visibility = View.VISIBLE
                    messageAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun listenRealtime() {
        if (chatroomId == null) return


        val query = firestore.collection("chatrooms")
            .document(chatroomId!!)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(15)


        query.addSnapshotListener { querySnapshot, error ->

            if (error != null) {
                if (binding.progressBar.visibility == View.VISIBLE) {
                    binding.progressBar.visibility = View.GONE
                }
                return@addSnapshotListener
            }

            if (querySnapshot != null && !querySnapshot.isEmpty) {
                if (binding.progressBar.visibility == View.VISIBLE) {
                    binding.progressBar.visibility = View.GONE
                }
                lastTimeStamp = querySnapshot.documents[querySnapshot.documents.size - 1].getTimestamp("timestamp")
                messageList.clear()
                for (sms in querySnapshot.documents) {
                    sms.toObject(ChatroomSmsModel::class.java)
                        ?.let { msg ->
                            messageList.add(msg)
                        }
                }

                messageAdapter.notifyDataSetChanged()

            } else {
                isLastPage = true
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
        val messagesCollection = db.collection("chatrooms")
            .document(chatroomId!!).collection("messages")
            .document()

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
            .set(messageObj)
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
        val userInChatRef = Firebase.firestore.collection("chatrooms")
            .document(chatroomId!!).collection("current_users")
            .document(currentUserUid)

        userInChatRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                userInChatRef.delete()
            }
        }
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> showAlertDialog()
            R.id.menu_users -> {
                showBottomSheet()
            }
        }
        return true
    }

    private fun showBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val bottomSheetBehavior: BottomSheetBehavior<View>
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_chat_activity, null)
        bottomSheetDialog.setContentView(view)

        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        val parentLayout = view.findViewById<LinearLayout>(R.id.parent_layout)
        assert(parentLayout != null)

        val progressBar: ProgressBar = parentLayout.findViewById(R.id.progress_bar)
        progressBar.visibility = VISIBLE

        val closeBtn = parentLayout.findViewById<ImageView>(R.id.image_close)
        val recCurrentUsers : RecyclerView = parentLayout.findViewById(R.id.recycler_view)

        CoroutineScope(Dispatchers.IO).launch {
            getCurrentUser(progressBar, recCurrentUsers)
        }

        closeBtn.setOnClickListener {
            bottomSheetDialog.dismiss()
        }


        parentLayout.minimumHeight = resources.displayMetrics.heightPixels / 2


        bottomSheetDialog.show()
    }

    private suspend fun getCurrentUser(progressBar: ProgressBar, recCurrentUsers: RecyclerView) {
        progressBar.visibility = VISIBLE
        withTimeout(5000){
            try {
                userList = ArrayList()
                Firebase.firestore.collection("chatrooms")
                    .document(chatroomId!!)
                    .collection("current_users")
                    .get().addOnSuccessListener { document ->
                        if (!document.isEmpty){
                            for (user in document.documents){
                                val usr = user.toObject(User::class.java)
                                userList.add(usr!!)
                            }
                        }

                        userDataAdapter = ChatroomUserAdapter(userList, applicationContext)
                        val layoutManager = LinearLayoutManager(applicationContext)
                        recCurrentUsers.layoutManager = layoutManager
                        recCurrentUsers.adapter= userDataAdapter



                        recCurrentUsers.visibility = VISIBLE
                        progressBar.visibility = GONE
                    }.addOnFailureListener {

                        MyUtils.showToast(applicationContext, it.message.toString())
                        progressBar.visibility = GONE
                    }

            }catch (e : TimeoutCancellationException) {
                MyUtils.showToast(applicationContext, e.message.toString())
            }
        }

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_chatroom_act, menu)
        return super.onCreateOptionsMenu(menu)
    }
}

