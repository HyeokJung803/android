package com.studyapp.android.data.api.model.repository

import com.google.gson.annotations.SerializedName

data class Photo(
    val photoId: Long,
    val groupId: Long,
    val userId: Long,
    val username: String?,
    @SerializedName("imageUrl")  // ✅ JSON 필드명 명시
    val imageUrl: String,
    val originalFilename: String?,
    val description: String?,
    val fileSize: Long?,
    val createdAt: String?
)