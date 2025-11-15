package com.studyapp.android.ui.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyapp.android.StudyApp
import com.studyapp.android.data.api.model.repository.PostRepository
import com.studyapp.android.data.api.model.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostViewModel : ViewModel() {
    private val repository = PostRepository()

    private val currentUserId: Long
        get() = StudyApp.currentUserId

    private val _postListState = MutableStateFlow<PostListState>(PostListState.Loading)
    val postListState: StateFlow<PostListState> = _postListState

    private val _postDetailState = MutableStateFlow<PostDetailState>(PostDetailState.Loading)
    val postDetailState: StateFlow<PostDetailState> = _postDetailState

    private val _createPostState = MutableStateFlow<CreatePostState>(CreatePostState.Idle)
    val createPostState: StateFlow<CreatePostState> = _createPostState

    // 게시글 목록 조회
    fun getPosts(groupId: Long, postType: PostType? = null) {
        viewModelScope.launch {
            _postListState.value = PostListState.Loading
            val result = repository.getPosts(groupId, postType)

            _postListState.value = if (result.isSuccess) {
                PostListState.Success(result.getOrNull()!!.posts)
            } else {
                PostListState.Error(result.exceptionOrNull()?.message ?: "게시글 조회 실패")
            }
        }
    }

    // 게시글 상세 조회
    fun getPostDetail(postId: Long) {
        viewModelScope.launch {
            _postDetailState.value = PostDetailState.Loading

            // ✅ currentUserId 전달
            val result = repository.getPostDetail(postId, currentUserId)

            _postDetailState.value = if (result.isSuccess) {
                PostDetailState.Success(result.getOrNull()!!)
            } else {
                PostDetailState.Error(result.exceptionOrNull()?.message ?: "게시글 조회 실패")
            }
        }
    }

    // 게시글 작성
    fun createPost(groupId: Long, title: String, content: String, postType: PostType) {
        viewModelScope.launch {
            _createPostState.value = CreatePostState.Loading

            val request = CreatePostRequest(title, content, postType)
            val result = repository.createPost(groupId, currentUserId, request)

            _createPostState.value = if (result.isSuccess) {
                CreatePostState.Success("게시글이 작성되었습니다")
            } else {
                CreatePostState.Error(result.exceptionOrNull()?.message ?: "게시글 작성 실패")
            }
        }
    }

    // ✅ 게시글 삭제 (groupId 파라미터 추가)
    fun deletePost(postId: Long, groupId: Long?) {
        viewModelScope.launch {
            val result = repository.deletePost(postId, currentUserId, groupId)

            if (result.isSuccess) {
                // 삭제 후 상세 화면 닫기 처리
                _postDetailState.value = PostDetailState.Deleted
            } else {
                // ✅ 에러 메시지 표시
                _postDetailState.value = PostDetailState.Error(
                    result.exceptionOrNull()?.message ?: "게시글 삭제 실패"
                )
            }
        }
    }

    // 댓글 작성
    fun createComment(postId: Long, content: String) {
        viewModelScope.launch {
            val request = CreateCommentRequest(content)
            val result = repository.createComment(postId, currentUserId, request)

            if (result.isSuccess) {
                // 댓글 작성 후 상세 조회 새로고침
                getPostDetail(postId)
            } else {
                _postDetailState.value = PostDetailState.Error(
                    result.exceptionOrNull()?.message ?: "댓글 작성 실패"
                )
            }
        }
    }

    // ✅ 댓글 삭제 (groupId 파라미터 추가)
    fun deleteComment(postId: Long, commentId: Long, groupId: Long?) {
        viewModelScope.launch {
            val result = repository.deleteComment(commentId, currentUserId, groupId)

            if (result.isSuccess) {
                // 댓글 삭제 후 상세 조회 새로고침
                getPostDetail(postId)
            } else {
                // ✅ 에러 메시지 표시
                _postDetailState.value = PostDetailState.Error(
                    result.exceptionOrNull()?.message ?: "댓글 삭제 실패"
                )
            }
        }
    }

    fun resetCreatePostState() {
        _createPostState.value = CreatePostState.Idle
    }
}

// State 정의
sealed class PostListState {
    object Loading : PostListState()
    data class Success(val posts: List<Post>) : PostListState()
    data class Error(val message: String) : PostListState()
}

sealed class PostDetailState {
    object Loading : PostDetailState()
    data class Success(val post: PostDetail) : PostDetailState()
    data class Error(val message: String) : PostDetailState()
    object Deleted : PostDetailState()  // ✅ 삭제 완료 상태 추가
}

sealed class CreatePostState {
    object Idle : CreatePostState()
    object Loading : CreatePostState()
    data class Success(val message: String) : CreatePostState()
    data class Error(val message: String) : CreatePostState()
}