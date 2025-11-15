package com.studyapp.android.data.api.model.repository

import com.studyapp.android.data.api.model.repository.RetrofitClient.apiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GroupRepository {

    private val apiService = RetrofitClient.apiService

    suspend fun createGroup(request: GroupRequest, userId: Long): Result<GroupResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createGroup(request, userId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("그룹 생성 실패"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getAllGroups(userId: Long): Result<GroupListResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllGroups(userId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("그룹 조회 실패"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getGroupsByCategory(category: String, userId: Long): Result<GroupListResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getGroupsByCategory(category, userId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("카테고리별 조회 실패"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getMyGroups(userId: Long): Result<GroupListResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMyGroups(userId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("내 그룹 조회 실패"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun kickMember(
        groupId: Long,
        leaderId: Long,
        targetUserId: Long
    ): Result<ApiResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.kickMember(groupId, targetUserId, leaderId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("멤버 강퇴 실패"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun joinGroup(groupId: Long, userId: Long): Result<ApiResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = ApiService.JoinGroupRequest("")  // 빈 가입인사
                val response = apiService.joinGroup(groupId, userId, request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("그룹 가입 실패"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun leaveGroup(groupId: Long, userId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.leaveGroup(groupId, userId)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("그룹 탈퇴 실패"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getGroupDetail(groupId: Long, userId: Long): Result<GroupDetailResponse> {
        return try {
            val response = apiService.getGroupDetail(groupId, userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("그룹 정보를 불러올 수 없습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun joinGroupWithGreeting(
        groupId: Long,
        userId: Long,
        greeting: String
    ): Result<ApiResponse> {
        return try {
            val request = ApiService.JoinGroupRequest(greeting)
            val response = apiService.joinGroup(groupId, userId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("그룹 가입에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
