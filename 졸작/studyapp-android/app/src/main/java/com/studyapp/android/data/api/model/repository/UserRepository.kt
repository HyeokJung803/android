package com.studyapp.android.data.api.model.repository

import com.studyapp.android.ui.home.UserProfileData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository {
    private val apiService = RetrofitClient.apiService

    // 프로필 조회
    suspend fun getUserProfile(userId: Long): Result<UserProfileData> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserProfile(userId)
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    val profile = UserProfileData(
                        userId = data.userId,
                        email = data.email,
                        name = data.name,
                        nickname = data.nickname,
                        birthDate = data.birthDate,
                        profileImage = data.profileImage,
                        bio = data.bio,
                        createdAt = data.createdAt,
                        postCount = data.postCount,
                        commentCount = data.commentCount,
                        photoCount = data.photoCount,
                        groupCount = data.groupCount
                    )
                    Result.success(profile)
                } else {
                    Result.failure(Exception("프로필 조회 실패"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // 프로필 수정
    suspend fun updateProfile(
        userId: Long,
        nickname: String?,
        bio: String?
    ): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = UpdateProfileRequest(nickname, bio, null)
                val response = apiService.updateProfile(userId, request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("프로필 수정 실패"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // 비밀번호 변경
    suspend fun changePassword(
        userId: Long,
        currentPassword: String,
        newPassword: String
    ): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = ChangePasswordRequest(currentPassword, newPassword)
                val response = apiService.changePassword(userId, request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("비밀번호 변경 실패"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

// DTO
data class UpdateProfileRequest(
    val nickname: String?,
    val bio: String?,
    val profileImage: String?
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class UserStatsResponse(
    val userId: Long,
    val email: String,
    val name: String,
    val nickname: String,
    val birthDate: String,
    val profileImage: String?,
    val bio: String?,
    val createdAt: String,
    val postCount: Int,
    val commentCount: Int,
    val photoCount: Int,
    val groupCount: Int
)