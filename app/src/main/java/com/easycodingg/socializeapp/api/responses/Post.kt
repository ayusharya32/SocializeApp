package com.easycodingg.socializeapp.api.responses

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Post(
        @SerializedName("_id")
    val postId: String,
        val userId: String,
        val postImageUrl: String,
        val caption: String,
        var likedBy: MutableList<String>,
        val createdAt: Long,
        val postUser: User,
        var isLiked: Boolean = false
): Parcelable
