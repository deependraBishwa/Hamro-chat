package com.deepdev.hamrochat.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.deepdev.hamrochat.R
import com.deepdev.hamrochat.activities.CreateChatroomActivity
import com.deepdev.hamrochat.adapters.ChatroomAdapter
import com.deepdev.hamrochat.databinding.FragmentChatroomBinding
import com.deepdev.hamrochat.model.ChatroomModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class ChatroomFragment : Fragment() {
    private lateinit var viewPager: ViewPager2
    private lateinit var fab: FloatingActionButton
    private var isFabVisible = true
    private val binding by lazy { FragmentChatroomBinding.inflate(layoutInflater) }

    private lateinit var model : ArrayList<ChatroomModel>
    private lateinit var adapter : ChatroomAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        viewPager = requireActivity().findViewById(R.id.viewPager)
        fab = requireActivity().findViewById(R.id.fab_create_chat_room)
        fab.visibility = View.VISIBLE

        setUpViewPagerForFab()

        recyclerViewSetup()

        fabClick()




//todo work on chatroom creation

        return binding.root
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
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Handle any errors that may occur during the fetch
            }
    }


    private fun fabClick() {
        fab.setOnClickListener{
           val intent = Intent(requireActivity(), CreateChatroomActivity::class.java)
            startActivity(intent)
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

    private fun setUpViewPagerForFab() {

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 1) {
                    if (!isFabVisible) {
                        slideFabUp()
                    }
                } else {
                    if (isFabVisible) {
                        slideDownFab()
                    }
                }
            }
        })
    }

    private fun slideDownFab() {
        fab.animate()
            .translationY(fab.height.toFloat())
            .alpha(0f)
            .setListener(null)
            .setDuration(300)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isFabVisible = false
                }
            })
    }

    private fun slideFabUp() {
        fab.animate()
            .translationY(0f)
            .alpha(1f)
            .setListener(null)
            .setDuration(300)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isFabVisible = true
                }
            })
    }

}