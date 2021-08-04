package com.easycodingg.socializeapp.api.responses

data class AuthErrorResponse(
        val name: String? = "",
        val email: String? = "",
        val password: String? = ""
)
