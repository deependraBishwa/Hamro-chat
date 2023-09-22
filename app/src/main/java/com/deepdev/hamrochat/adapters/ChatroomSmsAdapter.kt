package com.deepdev.hamrochat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.deepdev.hamrochat.R
import com.deepdev.hamrochat.model.ChatroomSmsModel
import de.hdodenhof.circleimageview.CircleImageView

class ChatroomSmsAdapter(private val list : ArrayList<ChatroomSmsModel>,
                                            private val context : Context , private val currentUser : String)
            : RecyclerView.Adapter<ChatroomSmsAdapter.ChatroomSmsViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatroomSmsViewHolder {
       return ChatroomSmsViewHolder(LayoutInflater.from(context).inflate(R.layout.item_sms_chatroom, parent, false))
    }

    override fun getItemCount(): Int {
      return if (list.size !=0) list.size else 0
    }

    override fun onBindViewHolder(holder: ChatroomSmsViewHolder, position: Int) {
        val model = list[position]
        if (model.authorUid != currentUser){

            showSenderLayout(model, holder)
        }else{
            showReceiverLayout(model, holder)
        }

    }

    private fun showReceiverLayout(model: ChatroomSmsModel, holder: ChatroomSmsViewHolder) {
        holder.senderLayout.visibility = View.GONE
        holder.receiverLayout.visibility = View.VISIBLE
        holder.receiverMessage.text = model.message
        holder.receiverUserName.text = model.authorUsername
        Glide.with(context).load(model.authorImage)
            .into(holder.receiverImageView)

    }

    private fun showSenderLayout(model: ChatroomSmsModel, holder: ChatroomSmsViewHolder) {
        holder.receiverLayout.visibility = View.GONE
        holder.senderLayout.visibility = View.VISIBLE
        holder.senderMessage.text = model.message
        holder.senderUserName.text = model.authorUsername
        Glide.with(context).load(model.authorImage)
            .into(holder.senderImageView)
    }


    class ChatroomSmsViewHolder(itemView : View) : ViewHolder(itemView){
        val senderLayout : LinearLayout = itemView.findViewById(R.id.sender_layout)
        val senderMessage : TextView = itemView.findViewById(R.id.sender_message)
        val senderUserName : TextView = itemView.findViewById(R.id.sender_user_name)
        val senderImageView : CircleImageView = itemView.findViewById(R.id.sender_image_view)

        val receiverLayout : LinearLayout = itemView.findViewById(R.id.receiver_layout)
        val receiverMessage : TextView = itemView.findViewById(R.id.receiver_message)
        val receiverUserName : TextView = itemView.findViewById(R.id.receiver_username)
        val receiverImageView : CircleImageView = itemView.findViewById(R.id.receiver_image_view)

    }
}