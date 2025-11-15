package com.studyapp.android.data.api.model.repository

// ✅ Post 모델 수정 (목록용)
data class Post(
    val postId: Long,
    val title: String,
    val content: String,
    val postType: PostType,
    val username: String,
    val userId: Long,
    val createdAt: String,
    val commentCount: Int,
    val groupId: Long  // ✅ 추가
)

// ✅ PostDetail 모델 수정 (상세용)
data class PostDetail(
    val postId: Long,
    val title: String,
    val content: String,
    val postType: PostType,
    val username: String,
    val userId: Long,
    val createdAt: String,
    val comments: List<Comment>,
    val groupId: Long,      // ✅ 추가
    val isLeader: Boolean   // ✅ 추가 (현재 사용자가 그룹장인지)
)

// Comment 모델 (기존 유지)
data class Comment(
    val commentId: Long,
    val content: String,
    val username: String,
    val userId: Long,
    val createdAt: String
)

// PostType enum (기존 유지)
enum class PostType(val displayName: String) {
    FREE("자유 게시글"),
    NOTICE("공지사항");

    companion object {
        fun fromString(value: String): PostType {
            return when (value.uppercase()) {
                "FREE" -> FREE
                "NOTICE" -> NOTICE
                else -> FREE
            }
        }
    }
}

// API 응답용 래퍼
data class PostListResponse(
    val posts: List<Post>
)

// 게시글 작성 요청
data class CreatePostRequest(
    val title: String,
    val content: String,
    val postType: PostType
)

// 댓글 작성 요청
data class CreateCommentRequest(
    val content: String
)

data class UpdatePostRequest(
    val title: String,
    val content: String,
    val postType: PostType
)