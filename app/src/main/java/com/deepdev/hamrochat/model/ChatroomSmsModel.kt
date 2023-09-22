package com.deepdev.hamrochat.model

import com.google.firebase.firestore.FieldValue

data class ChatroomSmsModel(
    val message: String? = "",
    val timestamp: Any? = FieldValue.serverTimestamp(),
    val authorUid: String? = "",
    val messageId: String? = "",
    val authorUsername: String? = "",
    val authorImage : String?=""
)
