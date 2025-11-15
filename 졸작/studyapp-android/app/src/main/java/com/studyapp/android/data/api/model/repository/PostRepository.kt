package com.studyapp.android.data.api.model.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PostRepository {

    private val apiService = RetrofitClient.apiService

    // 게시글 목록 조회
    suspend fun getPosts(groupId: Long, postType: PostType? = null): Result<PostListResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = if (postType != null) {
                    apiService.getPostsByType(groupId, postType.name)
                } else {
                    apiService.getPosts(groupId)
                }

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("게시글 조회 실패"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ✅ 게시글 상세 조회 (userId 전달)
    suspend fun getPostDetail(postId: Long, userId: Long): Result<PostDetail> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPostDetail(postId, userId)

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("게시글 조회 실패"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // 게시글 작성
    suspend fun createPost(
        groupId: Long,
        userId: Long,
        request: CreatePostRequest
    ): Result<ApiResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createPost(groupId, userId, request)

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("게시글 작성 실패"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ✅ 게시글 삭제 (groupId 전달)
    suspend fun deletePost(
        postId: Long,
        userId: Long,
        groupId: Long?
    ): Result<ApiResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deletePost(postId, userId, groupId)

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    // ✅ 에러 메시지 파싱
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception(errorBody ?: "게시글 삭제 실패"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // 댓글 작성
    suspend fun createComment(
        postId: Long,
        userId: Long,
        request: CreateCommentRequest
    ): Result<ApiResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createComment(postId, userId, request)

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("댓글 작성 실패"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ✅ 댓글 삭제 (groupId 전달)
    suspend fun deleteComment(
        commentId: Long,
        userId: Long,
        groupId: Long?
    ): Result<ApiResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteComment(commentId, userId, groupId)

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    // ✅ 에러 메시지 파싱
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception(errorBody ?: "댓글 삭제 실패"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}