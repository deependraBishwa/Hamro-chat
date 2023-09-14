package com.deepdev.hamrochat.model

data class ChatroomModel(
    val adminUid : String? = "",
    val chatroomCreatedDate : Long?=0,
    val chatroomId : String?="",
    val chatroomImage : String?="",
    val chatroomName : String?="",
    val country : String?="",
    val welcomeMessage : String?=""
)
