package com.studyapp.android.data.api.model.repository

data class SignupRequest(
    val email: String,
    val password: String,
    val name: String,
    val nickname: String,
    val birthDate: String  // "2000-01-01" 형식
)

data class LoginRequest(
    val email: String,
    val password: String
)