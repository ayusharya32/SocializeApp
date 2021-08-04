package com.easycodingg.socializeapp.api.responses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val userId: String,
    val name: String,
    val email: String,
    val profilePhotoUrl: String,
    val following: List<String>,
    val followers: Int
): Parcelable