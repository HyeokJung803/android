package com.studyapp.android.data.api.model.repository

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val userId: Long?,
    val nickname: String?
)