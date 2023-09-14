package com.deepdev.hamrochat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.deepdev.hamrochat.R
import com.deepdev.hamrochat.model.CommentModel
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.TimeZone


class CommentAdapter(private val list : ArrayList<CommentModel>, private val context: Context)
    : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {


    class CommentViewHolder (val item : View) : ViewHolder(item){
        val authorImage : ImageView = item.findViewById(R.id.image_view_comment)
        val tvName : TextView = item.findViewById(R.id.tv_author_name)
        val tvComment : TextView = item.findViewById(R.id.tv_comment_text)
        val tvCreatedDate : TextView = item.findViewById(R.id.tv_time_ago)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment,  parent, false)
        return CommentViewHolder(view)
    }

    override fun getItemCount(): Int {
       return if (list.size != 0) list.size else 0
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


    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val model = list[position]

        holder.tvComment.text= model.comment
        holder.tvCreatedDate.text =getDate( model.date)

        getCommentorDetails(model.commentBy, holder)


    }

    private fun getCommentorDetails(commentBy: String?, holder: CommentViewHolder) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")

        usersCollection.document(commentBy.toString())
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val name = documentSnapshot.getString("name") ?: ""
                    val image = documentSnapshot.getString("image") ?: ""

                    holder.tvName.text = name
                    Glide.with(context).load(image).placeholder(R.drawable.user_place_holder).into(holder.authorImage)
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occur during the fetch
            }


    }

}