package com.deepdev.hamrochat.model

import com.google.firebase.Timestamp

data class ForyouModel(
    val text: String = "",
    val image: String = "",
    val postId: String = "",
    val authorId: String = "",
    val timestamp: Timestamp? = null
)