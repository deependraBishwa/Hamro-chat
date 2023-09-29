package com.deepdev.hamrochat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.deepdev.hamrochat.R
import com.deepdev.hamrochat.model.ChatroomSmsModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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

            holder.senderMessage.setTextColor(holder.getColor(model.authorUid.toString(), context))
            showSenderLayout(model, holder)
        }else{
            showReceiverLayout(model, holder)
        }

    }

    private fun showReceiverLayout(model: ChatroomSmsModel, holder: ChatroomSmsViewHolder) {
        holder.receiverCard.visibility = View.VISIBLE
        holder.senderCard.visibility = View.GONE
        holder.senderLayout.visibility = View.GONE
        holder.receiverLayout.visibility = View.VISIBLE
        holder.receiverMessage.text = model.message

        Firebase.firestore.collection("users").document(model.authorUid.toString())
            .addSnapshotListener { value, error ->
                if (error != null) {
                    // Handle the error appropriately (e.g., log or display a message)
                    return@addSnapshotListener
                }

                if (value != null && value.exists()) {
                    val username = value.getString("username") ?: ""
                    val image = value.getString("imageUrl") ?: ""

                    // Load image using Glide
                    Glide.with(context)
                        .load(image)
                        .placeholder(R.drawable.user_place_holder)
                        .into(holder.receiverImageView)

                    holder.receiverUserName.text = username
                } else {
                    // Handle the case where the user document doesn't exist
                }
            }
    }


    private fun showSenderLayout(model: ChatroomSmsModel, holder: ChatroomSmsViewHolder) {
        holder.receiverCard.visibility = View.GONE
        holder.senderCard.visibility = View.VISIBLE
        holder.receiverLayout.visibility = View.GONE
        holder.senderLayout.visibility = View.VISIBLE
        holder.senderMessage.text = model.message

        Firebase.firestore.collection("users")
            .document(model.authorUid.toString())
            .addSnapshotListener { value, error ->
                if (error != null) {
                    // Handle the error appropriately (e.g., log or display a message)
                    return@addSnapshotListener
                }

                if (value != null && value.exists()) {
                    val username = value.getString("username") ?: ""
                    val image = value.getString("imageUrl") ?: ""

                    // Load image using Glide
                    Glide.with(context)
                        .load(image)
                        .placeholder(R.drawable.user_place_holder)
                        .into(holder.senderImageView)

                    holder.senderUserName.text = username
                } else {
                    // Handle the case where the user document doesn't exist
                }
            }
    }


    class ChatroomSmsViewHolder(itemView : View) : ViewHolder(itemView){
        val senderCard : CardView = itemView.findViewById(R.id.sender_card)
        val senderLayout : LinearLayout = itemView.findViewById(R.id.sender_layout)
        val senderMessage : TextView = itemView.findViewById(R.id.sender_message)
        val senderUserName : TextView = itemView.findViewById(R.id.sender_user_name)
        val senderImageView : CircleImageView = itemView.findViewById(R.id.sender_image_view)

        val receiverCard : CardView = itemView.findViewById(R.id.receiver_card)
        val receiverLayout : LinearLayout = itemView.findViewById(R.id.receiver_layout)
        val receiverMessage : TextView = itemView.findViewById(R.id.receiver_message)
        val receiverUserName : TextView = itemView.findViewById(R.id.receiver_username)
        val receiverImageView : CircleImageView = itemView.findViewById(R.id.receiver_image_view)


        fun getColor(uid: String, context: Context): Int {
            return when(uid.substring(0, 1).lowercase()){
                "a" -> ContextCompat.getColor(context, R.color.color1)
                "b" -> ContextCompat.getColor(context, R.color.color2)
                "c" -> ContextCompat.getColor(context, R.color.color3)
                "d" -> ContextCompat.getColor(context, R.color.color4)
                "e" -> ContextCompat.getColor(context, R.color.color5)
                "f" -> ContextCompat.getColor(context, R.color.color6)
                "g" -> ContextCompat.getColor(context, R.color.color7)
                "h" -> ContextCompat.getColor(context, R.color.color8)
                "i" -> ContextCompat.getColor(context, R.color.color9)
                "j" -> ContextCompat.getColor(context, R.color.color10)
                "k" -> ContextCompat.getColor(context, R.color.color11)
                "l" -> ContextCompat.getColor(context, R.color.color12)
                "m" -> ContextCompat.getColor(context, R.color.color13)
                "n" -> ContextCompat.getColor(context, R.color.color14)
                "o" -> ContextCompat.getColor(context, R.color.color15)
                "p" -> ContextCompat.getColor(context, R.color.color16)
                "q" -> ContextCompat.getColor(context, R.color.color17)
                "r" -> ContextCompat.getColor(context, R.color.color18)
                "s" -> ContextCompat.getColor(context, R.color.color19)
                "t" -> ContextCompat.getColor(context, R.color.color20)
                "u" -> ContextCompat.getColor(context, R.color.color21)
                "v" -> ContextCompat.getColor(context, R.color.color22)
                "w" -> ContextCompat.getColor(context, R.color.color23)
                "x" -> ContextCompat.getColor(context, R.color.color24)
                "y" -> ContextCompat.getColor(context, R.color.color25)
                "z" -> ContextCompat.getColor(context, R.color.color26)
                else -> {
                    ContextCompat.getColor(context, R.color.color26)}
            }
        }
    }
}