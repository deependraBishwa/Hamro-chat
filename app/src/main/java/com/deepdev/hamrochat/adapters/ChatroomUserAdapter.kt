package com.deepdev.hamrochat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.deepdev.hamrochat.R
import com.deepdev.hamrochat.model.User
import com.deepdev.hamrochat.utils.ButtonStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView


class ChatroomUserAdapter(
    private var listOfUser: ArrayList<User>,
    private val context: Context
) : RecyclerView.Adapter<ChatroomUserAdapter.ViewHolder>() {
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_bottom_sheet_chat_activity, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = listOfUser[position]

        checkStatus(user.uid, holder.addFriend)


        Glide.with(context).load(user.imageUrl).placeholder(R.drawable.user_place_holder)
            .into(holder.imageView)

        holder.username.text = user.username

        holder.addFriend.setOnClickListener {
            sendRequest(user, holder)
        }
    }

    private fun sendRequest(user: User, holder: ViewHolder) {

        val requestRef = Firebase.firestore.collection("requests")
            .document(currentUserId!! + user.uid)

        val btnText = holder.addFriend.text.toString()

        val requestMap = hashMapOf(
            "from" to currentUserId,
            "to" to user.uid,
            "timestamp" to FieldValue.serverTimestamp(),
            "status" to "request"
        )

        when (btnText) {
            ButtonStatus.ADD -> {
                holder.addFriend.text = ButtonStatus.CANCEL
                requestRef.set(requestMap)
            }

            ButtonStatus.CANCEL -> {
                holder.addFriend.text = ButtonStatus.ADD
                requestRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        requestRef.delete()
                    }
                }
            }

            ButtonStatus.ACCEPT -> {
                holder.addFriend.text = ButtonStatus.FRIEND
                requestRef.get().addOnSuccessListener { document ->
                    if (document.exists()){
                        requestMap["status"] = "friend"
                        requestRef.set(requestMap)
                    }
                }
            }
            ButtonStatus.FRIEND -> {
                holder.addFriend.text = ButtonStatus.ADD
                requestRef.get().addOnSuccessListener { document ->
                    if (document.exists()){
                        requestRef.delete()
                    }
                }
            }
            else -> {}
        }

    }

    private fun checkStatus(uid: String, btnAd: TextView) {

        Firebase.firestore.collection("requests")
            .document(currentUserId!! + uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    when (document.getString("status")) {
                        "request" -> {
                            when (document.id) {
                                currentUserId + uid -> {// outgoing request
                                    btnAd.text = ButtonStatus.CANCEL
                                }

                                uid + currentUserId -> { // incoming request
                                    btnAd.text = ButtonStatus.ACCEPT
                                }
                            }
                        }

                        "friend" -> {
                            btnAd.text = ButtonStatus.FRIEND
                        }
                    }
                    btnAd.isEnabled = true
                } else {
                    if (uid == currentUserId) {
                        btnAd.isEnabled = false
                        btnAd.text = "You"
                    } else {
                        btnAd.text = "Add"
                        btnAd.isEnabled = true
                    }

                }
            }
    }


    private fun isCurrentUser(uid: String): Boolean {
        return uid == currentUserId
    }


    fun setData(list: ArrayList<User>) {

        this.listOfUser = list
        notifyDataSetChanged()

    }

    override fun getItemCount(): Int = if (listOfUser.size > 0) listOfUser.size else 0
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        val imageView: CircleImageView = itemView.findViewById(R.id.cv_user_image)
        val username: TextView = itemView.findViewById(R.id.tv_username)
        val addFriend: Button = itemView.findViewById(R.id.btn_add_as_friend)
    }

}