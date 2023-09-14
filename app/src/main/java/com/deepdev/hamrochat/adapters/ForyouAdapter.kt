package com.deepdev.hamrochat.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.deepdev.hamrochat.R
import com.deepdev.hamrochat.activities.CreateCommentsActivity
import com.deepdev.hamrochat.model.ForyouModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

class ForyouAdapter(var list: ArrayList<ForyouModel>, val context: Context) : RecyclerView.Adapter<ForyouAdapter.ForyouViewHolder>() {


    val currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()

    class ForyouViewHolder(itemview: View) : ViewHolder(itemview) {

        val tvPost: TextView = itemview.findViewById(R.id.post_text)
        val ivPost: ImageView = itemview.findViewById(R.id.iv_post_image)
        val btnLike: TextView = itemview.findViewById(R.id.btn_like_post)
        val likeCount: TextView = itemview.findViewById(R.id.like_count)
        val commentCount: TextView = itemview.findViewById(R.id.commentCount)
        val btnComment: TextView = itemview.findViewById(R.id.btn_comment)
        val tvTimestamp: TextView = itemview.findViewById(R.id.tv_time_stamp)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForyouViewHolder {
        return ForyouViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ForyouViewHolder, position: Int) {
        val model = list[position]
        model.postId?.let { checkIfPostIsLiked(it, holder.btnLike) }

        if (isImageEmpty(model.image)) {
            Glide.with(context).load(model.image).placeholder(R.drawable.ic_image_place_holder)
                .into(holder.ivPost)
        } else {
            holder.ivPost.visibility = View.GONE
        }
        holder.tvPost.text = model.text
        holder.tvTimestamp.text = getDate(model.timestamp?.seconds)


        holder.btnLike.setOnClickListener {
            likePost(model.postId!!, holder.btnLike)
        }



        holder.btnComment.setOnClickListener {
            val intent = Intent(context, CreateCommentsActivity::class.java)
            intent.putExtra("postId", model.postId)
            context.startActivity(intent)
        }

        countLikes(holder.likeCount, model.postId)

        countComments(holder.commentCount, model.postId)
    }

    private fun getDate(dateCreated: Long?): String {
        val userTimeZone = TimeZone.getDefault()
        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - (dateCreated ?: 0)


        val commentDate = when {
            timeDifference < 60 * 1000 -> "a few seconds ago"
            timeDifference < 60 * 60 * 1000 -> "${timeDifference / (60 * 1000)} minutes ago"
            timeDifference < 24 * 60 * 60 * 1000 -> "${timeDifference / (60 * 60 * 1000)} hours ago"
            timeDifference < 365 * 24 * 60 * 60 * 1000 -> {
                val userCalendar = Calendar.getInstance(userTimeZone)
                userCalendar.timeInMillis = dateCreated ?: 0
                val day = userCalendar.get(Calendar.DAY_OF_MONTH)
                val month = userCalendar.get(Calendar.MONTH) + 1 // Month is zero-based
                "$month/$day"
            }

            else -> {
                val userCalendar = Calendar.getInstance(userTimeZone)
                userCalendar.timeInMillis = dateCreated ?: 0
                val day = userCalendar.get(Calendar.DAY_OF_MONTH)
                val month = userCalendar.get(Calendar.MONTH) + 1 // Month is zero-based
                val year = userCalendar.get(Calendar.YEAR)
                "$day/$month/$year"
            }
        }
        return commentDate
    }

    private fun countComments(commentCount: TextView, postId: String?) {



    }


    private fun countLikes(likeCount: TextView, postId: String?) {


        val likeCountRef = FirebaseDatabase.getInstance().getReference("likes")
            .child(postId!!)


        likeCountRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val countOfLike = snapshot.childrenCount
                if (countOfLike > 0) {
                    likeCount.visibility = View.VISIBLE
                    likeCount.text = "$countOfLike Likes"
                } else {
                    likeCount.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun likePost(postId: String, btnLike: TextView) {

        CoroutineScope(Dispatchers.IO).launch {
            val likeRef = FirebaseDatabase.getInstance().getReference("likes").child(postId)
            likeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child(currentUser).exists()) {
                        btnColorUnliked(btnLike)
                        likeRef.child(currentUser).removeValue()
                            .addOnFailureListener {
                                btnColorLiked(btnLike)
                            }
                    } else {
                        btnColorLiked(btnLike)
                        likeRef.child(currentUser).setValue(true)
                            .addOnFailureListener {
                                btnColorUnliked(btnLike)
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }

            })
        }

    }


    private fun checkIfPostIsLiked(postId: String, btnLike: TextView) {

        CoroutineScope(Dispatchers.IO).launch {
            FirebaseDatabase.getInstance().getReference("likes").child(postId)
                .child(currentUser).addValueEventListener(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                btnColorLiked(btnLike)
                            } else {
                                btnColorUnliked(btnLike)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                        }

                    }
                )
        }

    }


    private fun isImageEmpty(postImage: String?): Boolean {
        if (postImage != "") {
            return true
        }
        return false
    }

    // like counting

    private fun btnColorUnliked(btnLike: TextView) {
        btnLike.setTextColor(Color.GRAY)
    }

    private fun btnColorLiked(btnLike: TextView) {
        btnLike.setTextColor(Color.RED)
    }


}