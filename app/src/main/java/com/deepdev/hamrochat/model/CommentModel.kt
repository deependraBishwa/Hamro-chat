package com.deepdev.hamrochat.model

import com.google.firebase.firestore.FieldValue

class CommentModel (
    val comment : String? = "",
    val commentId : String? = "",
    val commentBy : String? = "",
    val date :Any?=FieldValue.serverTimestamp()

)
