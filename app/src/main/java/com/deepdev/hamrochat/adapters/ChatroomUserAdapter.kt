package com.deepdev.hamrochat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.deepdev.hamrochat.R
import com.deepdev.hamrochat.model.UserDataModel
import de.hdodenhof.circleimageview.CircleImageView

class ChatroomUserAdapter(private var listOfUser : ArrayList<UserDataModel>,
    private val context : Context)
    : RecyclerView.Adapter<ChatroomUserAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_bottom_sheet_chat_activity
        , parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = listOfUser[position]

        Glide.with(context).load(user.imageUrl).placeholder(R.drawable.user_place_holder)
            .into(holder.imageView)

        holder.username.text = user.username
        holder.addFriend.setOnClickListener {

        }
    }

    fun setData(list : ArrayList<UserDataModel>){
        this.listOfUser = list
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int = if(listOfUser.size == null) 0 else listOfUser.size
    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){


        val imageView : CircleImageView= itemView.findViewById(R.id.cv_user_image)
        val username : TextView= itemView.findViewById(R.id.tv_username)
        val addFriend : TextView= itemView.findViewById(R.id.btn_add)
    }
}