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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar

class ForyouAdapter(var list: ArrayList<ForyouModel>, val context: Context) : RecyclerView.Adapter<ForyouAdapter.ForyouViewHolder>() {


    val currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()

    class ForyouViewHolder(itemview: View) : ViewHolder(itemview) {

        val tvPost: TextView = itemview.findViewById(R.id.post_text)
        val btnLike: TextView = itemview.findViewById(R.id.btn_like_post)
        val likeCount: TextView = itemview.findViewById(R.id.like_count)
        val commentCount: TextView = itemview.findViewById(R.id.commentCount)
        val btnComment: TextView = itemview.findViewById(R.id.btn_comment)
        val tvTimestamp: TextView = itemview.findViewById(R.id.tv_time_stamp)
        val ivAuthorImage: ImageView = itemview.findViewById(R.id.author_image)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForyouViewHolder {
        return ForyouViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ForyouViewHolder, position: Int) {
        val model = list[position]
        setAuthorImage(model, holder)

        setTimeStamp(model, holder)

        checkIfPostIsLiked(model.postId, holder.btnLike)

        holder.tvPost.text = model.text


        holder.btnLike.setOnClickListener {
            likePost(model.postId, holder.btnLike)
        }



        holder.btnComment.setOnClickListener {
            val intent = Intent(context, CreateCommentsActivity::class.java)
            intent.putExtra("postId", model.postId)
            context.startActivity(intent)
        }

        countLikes(holder.likeCount, model.postId)

        countComments(holder.commentCount, model.postId)
    }

    private fun setTimeStamp(model: ForyouModel, holder: ForyouViewHolder) {

        Firebase.firestore.collection("posts").document(model.postId)
            .get().addOnSuccessListener { document ->
                val timestamp = document.getTimestamp("timestamp")
                val formattedTimestamp = formatTimestamp(timestamp!!)

                holder.tvTimestamp.text = formattedTimestamp
            }
    }

    fun formatTimestamp(timestamp: Timestamp): String {
        val currentDate = Calendar.getInstance()
        val timestampDate = Calendar.getInstance()
        timestampDate.time = timestamp.toDate()

        val diffInMillis = currentDate.timeInMillis - timestampDate.timeInMillis
        val seconds = diffInMillis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val months = days / 30
        val years = days / 365

        return when {
            seconds < 60 -> "$seconds seconds ago"
            minutes < 60 -> "$minutes minutes ago"
            hours < 24 -> "$hours hours ago"
            days == 1L -> "Yesterday"
            days < 30 -> "$days days ago"
            months < 12 -> "$months months ago"
            else -> "$years years ago"
        }
    }

    private fun setAuthorImage(model: ForyouModel, holder: ForyouViewHolder) {

        Firebase.firestore.collection("users").document(model.authorId)
            .get().addOnSuccessListener { document ->
                Glide.with(context).load(document.getString("imageUrl"))
                    .placeholder(R.drawable.user_place_holder).into(holder.ivAuthorImage)
            }
    }


    private fun countComments(commentCount: TextView, postId: String?) {
        Firebase.firestore.collection("comments").document(postId!!)
            .collection("comment")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (value != null && value.documents.isNotEmpty()) {
                    commentCount.visibility = View.VISIBLE
                    val commentsCount = "${value.documents.size} Comments"
                    commentCount.text = commentsCount
                } else {
                    commentCount.visibility = View.INVISIBLE
                }
            }
    }


    private fun countLikes(likeCount: TextView, postId: String?) {


        val likeCountRef = Firebase.firestore.collection("likes").document(postId!!)
            .collection("like")

        likeCountRef.addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            if (value != null && value.documents.isNotEmpty()) {
                likeCount.visibility = View.VISIBLE
                likeCount.text = value.documents.size.toString() + " Likes"
            } else {
                likeCount.visibility = View.INVISIBLE
            }
        }

    }

    private fun likePost(postId: String, btnLike: TextView) {
        val emptyData = HashMap<String, Any>()
        val likeRef = Firebase.firestore.collection("likes").document(postId)
            .collection("like").document(currentUser)
        likeRef.get().addOnCompleteListener { task ->
            if (task.result.exists()) {
                likeRef.delete()
                btnColorUnliked(btnLike)

            } else {
                btnColorLiked(btnLike)
                likeRef.set(emptyData)

            }
        }

    }


    private fun checkIfPostIsLiked(postId: String, btnLike: TextView) {

        val likeRef = Firebase.firestore.collection("likes")
            .document(postId).collection("like")
            .document(currentUser)

        likeRef.addSnapshotListener { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (value != null && value.exists()) {
                btnColorLiked(btnLike)
            } else {
                btnColorUnliked(btnLike)
            }
        }
    }
}

private fun btnColorUnliked(btnLike: TextView) {
    btnLike.setTextColor(Color.GRAY)
}

private fun btnColorLiked(btnLike: TextView) {
    btnLike.setTextColor(Color.RED)
}
