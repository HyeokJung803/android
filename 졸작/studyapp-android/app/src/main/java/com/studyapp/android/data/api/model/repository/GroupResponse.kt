package com.studyapp.android.data.api.model.repository

import com.google.gson.annotations.SerializedName

data class GroupMember(
    val userId: Long,
    val username: String,
    val profileImage: String? = null,
    val isLeader: Boolean = false,
    val joinedAt: String,
    val isNew: Boolean = false
)

data class GroupResponse(
    val groupId: Long,
    val groupName: String,
    val description: String,
    val category: Category,
    val leaderId: Long,
    val leaderNickname: String,
    val maxMembers: Int,
    val currentMembers: Int,
    val createdAt: String,
    val isMember: Boolean
)

data class GroupDetailResponse(
    val groupId: Long,
    @SerializedName("name") val groupName: String,
    val description: String,
    val category: Category,
    val currentMembers: Int,
    val maxMembers: Int,
    val createdAt: String,
    @SerializedName("joined") val isJoined: Boolean,  // ✅ "joined"로 매핑
    @SerializedName("leader") val isLeader: Boolean,  // ✅ "leader"로 매핑 (추가!)
    val members: List<GroupMemberDetail>
)

data class GroupMemberDetail(
    val userId: Long,
    @SerializedName("nickname") val username: String?,
    val profileImage: String?,
    @SerializedName("leader") val isLeader: Boolean,
    val joinedAt: String?,
    val isNew: Boolean = false
)

data class ApiResponse(
    val success: Boolean,
    val message: String
)