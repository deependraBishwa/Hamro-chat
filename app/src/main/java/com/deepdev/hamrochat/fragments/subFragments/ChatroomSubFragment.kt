package com.deepdev.hamrochat.fragments.subFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.deepdev.hamrochat.adapters.ChatroomAdapter
import com.deepdev.hamrochat.databinding.FragmentChatroomSubBinding
import com.deepdev.hamrochat.model.ChatroomModel
import com.deepdev.hamrochat.utils.MyUtils
import com.google.firebase.firestore.FirebaseFirestore


class ChatroomSubFragment : Fragment() {
    private lateinit var model : ArrayList<ChatroomModel>
    private lateinit var adapter : ChatroomAdapter
    private val binding by lazy { FragmentChatroomSubBinding.inflate(layoutInflater) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = binding.root

        binding.progressBar.visibility = View.VISIBLE

        recyclerViewSetup()


        return view
    }
    private fun fetchDataFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val chatroomCollection = db.collection("chatrooms")

        chatroomCollection
            .get()
            .addOnSuccessListener { querySnapshot ->
                model.clear()
                for (document in querySnapshot.documents) {
                    val chatroom = document.toObject(ChatroomModel::class.java)
                    chatroom?.let { model.add(it) }
                }
                binding.progressBar.visibility = View.GONE
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                MyUtils.showToast(requireActivity() , exception.message.toString())
            }
    }
    private fun recyclerViewSetup() {

        model = ArrayList()


        adapter = ChatroomAdapter(requireActivity(), model)
        val layoutManager = LinearLayoutManager(requireActivity())

        binding.chatRoomRecyclerView.adapter = adapter
        binding.chatRoomRecyclerView.layoutManager = layoutManager
        fetchDataFromFirestore()
    }
}