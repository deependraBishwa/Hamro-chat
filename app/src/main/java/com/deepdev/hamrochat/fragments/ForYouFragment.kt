package com.deepdev.hamrochat.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.deepdev.hamrochat.activities.CreatePostActivity
import com.deepdev.hamrochat.adapters.ForyouAdapter
import com.deepdev.hamrochat.databinding.FragmentForYouBinding
import com.deepdev.hamrochat.model.ForyouModel
import com.deepdev.hamrochat.utils.MyProgressDialog
import com.deepdev.hamrochat.utils.MyUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class ForYouFragment : Fragment() {

    private lateinit var posts: ArrayList<ForyouModel>
    private lateinit var foryouPostsAdapter: ForyouAdapter
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val binding by lazy { FragmentForYouBinding.inflate(layoutInflater) }


    private lateinit var progressDialog: MyProgressDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = binding.root

        progressDialog = MyProgressDialog(requireActivity())

        recyclerViewSetup()


        binding.tvWhatsOnYourMind.setOnClickListener {
            startActivity(Intent(requireActivity(), CreatePostActivity::class.java))
        }

        return view
    }

    private fun recyclerViewSetup() {
        posts = ArrayList()
        foryouPostsAdapter = ForyouAdapter(posts, requireActivity())
        val layoutManager = LinearLayoutManager(requireActivity())

        binding.recyclerFeedPost.adapter = foryouPostsAdapter
        binding.recyclerFeedPost.layoutManager = layoutManager
        binding.recyclerFeedPost.setHasFixedSize(true)

        fetchDataFromFirebase()

    }

    private fun fetchDataFromFirebase() {

        progressDialog.show()
        firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    MyUtils.showToast(requireActivity(), error.message.toString())
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    posts.clear()
                    for (document in snapshot.documents) {
                        val post = document.toObject(ForyouModel::class.java)
                        posts.add(post!!)
                    }
                    notifyAdapter()
                } else {
                    progressDialog.hide()
                }
            }

    }

    private fun notifyAdapter() {
        foryouPostsAdapter.notifyDataSetChanged()
        progressDialog.hide()
    }


}