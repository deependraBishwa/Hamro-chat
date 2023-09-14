package com.deepdev.hamrochat.model

data class ChatroomSmsModel(
    val message: String? = "",
    val timestamp: Long? = 0,
    val authorUid: String? = "",
    val messageId: String? = "",
    val authorUsername: String? = "",
    val imageUrl : String?=""
)
