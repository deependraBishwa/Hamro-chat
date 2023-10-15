package com.deepdev.hamrochat.utils

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deepdev.hamrochat.R
import com.deepdev.hamrochat.adapters.ChatroomUserAdapter
import com.deepdev.hamrochat.model.User
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MyBottomSheetDialog(
    private val chatroomId: String
) :
    BottomSheetDialogFragment() {

    private lateinit var rootView: View
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: ChatroomUserAdapter


    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return bottomSheetDialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.bottom_sheet_chat_activity, container)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        view.minimumHeight = 500

        val closeBtn = view.findViewById<ImageView>(R.id.image_close)

        closeBtn.setOnClickListener {
            dismiss()
        }


        userCurrentlyInRoom()

    }

    private fun userCurrentlyInRoom() {
        userList = ArrayList()
        adapter = ChatroomUserAdapter(userList, requireActivity())

        val recyclerView = view?.findViewById<RecyclerView>(R.id.recycler_view)
        val layoutManager = LinearLayoutManager(requireActivity())
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = layoutManager

        val userInChatRef = Firebase.firestore.collection("user_live_in_chatroom")
            .document(chatroomId!!).collection("users")
        userInChatRef.addSnapshotListener { value, error ->
            if (error != null) {
                MyUtils.showToast(requireActivity(), error.message.toString())
                return@addSnapshotListener
            }

            if (value != null && !value.isEmpty) {
                userList.clear()
                for (userId in value.documents) {
                    Firebase.firestore.collection("users")
                        .document(userId.id)
                        .get().addOnSuccessListener {
                            val name = it.getString("name") ?: ""
                            val gender = it.getString("gender") ?: ""
                            val imageUrl = it.getString("imageUrl") ?: ""
                            val dob = it.getString("dateOfBirth") ?: ""
                            val uid = it.getString("uid") ?: ""
                            val username = it.getString("username") ?: ""
                            val userModel = User(dob, gender, imageUrl, name, uid, username)
                            userList.add(userModel)
                            if (userList.size == value.documents.size) {
                                Log.d("currents", "userCurrentlyInRoom: ${userId.id}")
                                adapter.notifyDataSetChanged()
                            }
                        }
                }
            }
        }
    }
}