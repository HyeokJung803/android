package com.studyapp.android.data.api.model.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatRepository {
    private val apiService = RetrofitClient.apiService

    // 전체 메시지 조회
    suspend fun getMessages(groupId: Long): Result<List<Message>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMessages(groupId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("메시지 조회 실패"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // 특정 시간 이후 메시지 조회 (Polling용)
    suspend fun getMessagesAfter(groupId: Long, after: String): Result<List<Message>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMessagesAfter(groupId, after)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("메시지 조회 실패"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // 메시지 전송
    suspend fun sendMessage(groupId: Long, userId: Long, content: String): Result<Message> {
        return withContext(Dispatchers.IO) {
            try {
                val request = SendMessageRequest(content)
                val response = apiService.sendMessage(groupId, userId, request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("메시지 전송 실패"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}