package com.deepdev.hamrochat.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.deepdev.hamrochat.R
import com.deepdev.hamrochat.activities.ChatroomActivity
import com.deepdev.hamrochat.model.ChatroomModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatroomAdapter (
                                    private val context : Context,
                                    private val list : ArrayList<ChatroomModel>?)
                                    : RecyclerView.Adapter<ChatroomAdapter
                                        .ChatroomViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatroomViewHolder {
       return ChatroomViewHolder(LayoutInflater.from(parent.context)
           .inflate(R.layout.item_chat_room , parent, false))
    }

    override fun onBindViewHolder(holder: ChatroomViewHolder, position: Int) {
       val model = list!![position]

        Glide.with(context).load(model.chatroomImage).placeholder(R.drawable.ic_image_place_holder)
            .into(holder.imageProfile)
        holder.chatroomName.text = model.chatroomName
        holder.tvWelcomeMessage.text = model.welcomeMessage

        holder.btnEnter.setOnClickListener {
            val intent = Intent(context, ChatroomActivity::class.java)
            intent.putExtra("chatroomId", model.chatroomId)
            intent.putExtra("chatroomName", model.chatroomName)
            context.startActivity(intent)
        }
        countNumberOfUserChatting(model.chatroomId, holder.countOfUser)
    }

    private fun countNumberOfUserChatting(chatroomId: String?, countOfUser: TextView) {
        Firebase.firestore.collection("user_live_in_chatroom").document(chatroomId!!)
            .collection("users").addSnapshotListener { value, error ->
                if (error != null){
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (value !=null && value.documents.isNotEmpty()){
                    countOfUser.text = value.documents.size.toString()+" peoples"
                }else{
                    countOfUser.visibility = View.INVISIBLE
                }
            }
    }

    override fun getItemCount(): Int {
       return if (list?.size ==null) 0 else list.size
    }

    class ChatroomViewHolder (item : View) : RecyclerView.ViewHolder(item){

        val chatroomName : TextView = item.findViewById(R.id.chatroom_name)
        val imageProfile : ImageView = item.findViewById(R.id.prof_image)
        val tvWelcomeMessage : TextView = item.findViewById(R.id.tv_welcome_message)
        val btnEnter : ImageView = item.findViewById(R.id.btn_enter)
        val countOfUser : TextView = item.findViewById(R.id.tv_count_of_user)
    }
}