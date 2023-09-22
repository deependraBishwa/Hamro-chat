package com.deepdev.hamrochat.model

import com.google.firebase.firestore.FieldValue

data class ChatroomModel(
    val adminUid : String? = "",
    val chatroomCreatedDate : Any=FieldValue.serverTimestamp(),
    val chatroomId : String?="",
    val chatroomImage : String?="",
    val chatroomName : String?="",
    val country : String?="",
    val welcomeMessage : String?=""
)
