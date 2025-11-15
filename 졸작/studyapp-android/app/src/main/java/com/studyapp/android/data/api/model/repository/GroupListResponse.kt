package com.studyapp.android.data.api.model.repository

data class GroupListResponse(
    val success: Boolean,
    val message: String,
    val groups: List<GroupResponse>
)