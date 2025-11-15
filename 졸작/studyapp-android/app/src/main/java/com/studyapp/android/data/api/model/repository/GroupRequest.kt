package com.studyapp.android.data.api.model.repository

data class GroupRequest(
    val groupName: String,
    val description: String,
    val category: Category,
    val maxMembers: Int
)