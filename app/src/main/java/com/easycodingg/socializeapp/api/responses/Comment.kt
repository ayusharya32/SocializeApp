package com.easycodingg.socializeapp.api.responses

data class Comment(
    val commentId: String,
    val commentText: String,
    val commentUser: User
)
