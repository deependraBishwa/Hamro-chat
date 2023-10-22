package com.deepdev.hamrochat.fragments.subFragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.deepdev.hamrochat.adapters.ChatroomAdapter
import com.deepdev.hamrochat.databinding.FragmentChatroomSub2Binding
import com.deepdev.hamrochat.model.ChatroomModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match

class ChatroomSubFragment2 : Fragment() {

    private val binding by lazy { FragmentChatroomSub2Binding.inflate(layoutInflater) }
    private lateinit var  chatroomList : ArrayList<ChatroomModel>
    private lateinit var chatroomAdapter: ChatroomAdapter
    private val currentUserUid by lazy { FirebaseAuth.getInstance().currentUser?.uid }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = binding.root

        setUpRecyclerView()


        return rootView
    }

    private fun setUpRecyclerView() {
        chatroomList = ArrayList()
        chatroomAdapter = ChatroomAdapter(requireActivity(), chatroomList)
        val layoutManager = LinearLayoutManager(requireActivity())

        binding.chatRoomRecyclerView.layoutManager = layoutManager
        binding.chatRoomRecyclerView.adapter=chatroomAdapter

        fetchRecentJoinedChatroom()


    }

    private fun fetchRecentJoinedChatroom() {
        Firebase.firestore.collection("users")
            .document(currentUserUid!!)
            .collection("recent_chatroom")
            .orderBy("joined_time",Query.Direction.DESCENDING)
            .limit(15)
            .addSnapshotListener { value, error ->
                if (error != null){
                    return@addSnapshotListener
                }
                var temp = ArrayList<String>()
                if (value != null){
                    chatroomList.clear()
                    if (!value.isEmpty){
                        for (document in value.documents){
                            val chatroomId = document.getString("chatroomId") ?: ""
                            temp.add(chatroomId)
                            Log.d("chatroomId", "fetchRecentJoinedChatroom: $chatroomId")

                        }
                        for (chatroomId in temp){
                            Firebase.firestore.collection("chatrooms")
                                .document(chatroomId)
                                .get().addOnSuccessListener {
                                    documentRef ->
                                    val chatroom = documentRef.toObject(ChatroomModel::class.java)
                                    chatroomList.add(chatroom!!)
                                    if (chatroomList.size == temp.size){
                                        chatroomAdapter.notifyDataSetChanged()
                                        binding.progressBar.visibility = View.GONE
                                    }
                                }
                        }

                    }else{

                    }
                }
            }
    }

    override fun onResume() {
        super.onResume()
        setUpRecyclerView()
    }
}