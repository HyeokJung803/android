package com.studyapp.android.data.api.model.repository

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ===== 인증 관련 =====
    @POST("api/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/auth/check-nickname")
    suspend fun checkNickname(@Query("nickname") nickname: String): Response<AuthResponse>

    // ===== 그룹 관련 =====
    @POST("api/groups")
    suspend fun createGroup(
        @Body request: GroupRequest,
        @Query("userId") userId: Long
    ): Response<GroupResponse>

    @GET("api/groups")
    suspend fun getAllGroups(
        @Query("userId") userId: Long
    ): Response<GroupListResponse>

    @GET("api/groups/category/{category}")
    suspend fun getGroupsByCategory(
        @Path("category") category: String,
        @Query("userId") userId: Long
    ): Response<GroupListResponse>

    @GET("api/groups/my-groups")
    suspend fun getMyGroups(
        @Query("userId") userId: Long
    ): Response<GroupListResponse>

    @POST("api/groups/{groupId}/join")
    suspend fun joinGroup(
        @Path("groupId") groupId: Long,
        @Query("userId") userId: Long,
        @Body request: JoinGroupRequest
    ): Response<ApiResponse>

    @DELETE("api/groups/{groupId}/leave")
    suspend fun leaveGroup(
        @Path("groupId") groupId: Long,
        @Query("userId") userId: Long
    ): Response<Unit>  // ✅ Unit으로 수정

    @DELETE("api/groups/{groupId}/members/{targetUserId}")
    suspend fun kickMember(
        @Path("groupId") groupId: Long,
        @Path("targetUserId") targetUserId: Long,
        @Query("leaderId") leaderId: Long
    ): Response<ApiResponse>

    @GET("api/groups/{groupId}/detail")
    suspend fun getGroupDetail(
        @Path("groupId") groupId: Long,
        @Query("userId") userId: Long
    ): Response<GroupDetailResponse>

    data class JoinGroupRequest(
        val greeting: String
    )

    // ===== 게시판 관련 =====
    @GET("api/groups/{groupId}/posts")
    suspend fun getPosts(
        @Path("groupId") groupId: Long
    ): Response<PostListResponse>

    @GET("api/posts/{postId}")
    suspend fun getPostDetail(
        @Path("postId") postId: Long,
        @Query("userId") userId: Long
    ): Response<PostDetail>

    @POST("api/groups/{groupId}/posts")
    suspend fun createPost(
        @Path("groupId") groupId: Long,
        @Query("userId") userId: Long,
        @Body request: CreatePostRequest
    ): Response<ApiResponse>

    @PUT("api/posts/{postId}")
    suspend fun updatePost(
        @Path("postId") postId: Long,
        @Query("userId") userId: Long,
        @Body request: UpdatePostRequest
    ): Response<Post>

    @DELETE("api/posts/{postId}")
    suspend fun deletePost(
        @Path("postId") postId: Long,
        @Query("userId") userId: Long,
        @Query("groupId") groupId: Long? = null  // ✅ 그룹장 권한용
    ): Response<ApiResponse>

    @POST("api/posts/{postId}/comments")
    suspend fun createComment(
        @Path("postId") postId: Long,
        @Query("userId") userId: Long,
        @Body request: CreateCommentRequest
    ): Response<ApiResponse>

    @DELETE("api/comments/{commentId}")
    suspend fun deleteComment(
        @Path("commentId") commentId: Long,
        @Query("userId") userId: Long,
        @Query("groupId") groupId: Long? = null  // ✅ 그룹장 권한용
    ): Response<ApiResponse>

    // ✅ 게시글 목록 조회 (타입별 필터링)
    @GET("api/groups/{groupId}/posts")
    suspend fun getPostsByType(
        @Path("groupId") groupId: Long,
        @Query("postType") postType: String  // "FREE" 또는 "NOTICE"
    ): Response<PostListResponse>

    // ===== 사진첩 관련 =====
    @Multipart
    @POST("api/groups/{groupId}/photos")
    suspend fun uploadPhoto(
        @Path("groupId") groupId: Long,
        @Query("userId") userId: Long,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody?
    ): Response<Photo>

    @GET("api/groups/{groupId}/photos")
    suspend fun getGroupPhotos(
        @Path("groupId") groupId: Long
    ): Response<List<Photo>>

    @DELETE("api/photos/{photoId}")
    suspend fun deletePhoto(
        @Path("photoId") photoId: Long,
        @Query("userId") userId: Long,
        @Query("groupId") groupId: Long? = null  // ✅ 그룹장 권한용
    ): Response<ApiResponse>

    // ===== 채팅 관련 =====
    @GET("api/groups/{groupId}/messages")
    suspend fun getMessages(
        @Path("groupId") groupId: Long
    ): Response<List<Message>>

    @GET("api/groups/{groupId}/messages/after")
    suspend fun getMessagesAfter(
        @Path("groupId") groupId: Long,
        @Query("after") after: String
    ): Response<List<Message>>

    @POST("api/groups/{groupId}/messages")
    suspend fun sendMessage(
        @Path("groupId") groupId: Long,
        @Query("userId") userId: Long,
        @Body request: SendMessageRequest
    ): Response<Message>

    // ===== 사용자 프로필 관련 =====
    @GET("api/users/{userId}/profile")
    suspend fun getUserProfile(
        @Path("userId") userId: Long
    ): Response<UserStatsResponse>

    @PUT("api/users/{userId}/profile")
    suspend fun updateProfile(
        @Path("userId") userId: Long,
        @Body request: UpdateProfileRequest
    ): Response<AuthResponse>

    @PUT("api/users/{userId}/password")
    suspend fun changePassword(
        @Path("userId") userId: Long,
        @Body request: ChangePasswordRequest
    ): Response<AuthResponse>


}