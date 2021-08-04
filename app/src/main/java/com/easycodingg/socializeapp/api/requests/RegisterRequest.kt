package com.easycodingg.socializeapp.api.requests

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)
