package com.easycodingg.socializeapp.api.requests

data class AddCommentRequest(
    val postId: String,
    val commentText: String
)