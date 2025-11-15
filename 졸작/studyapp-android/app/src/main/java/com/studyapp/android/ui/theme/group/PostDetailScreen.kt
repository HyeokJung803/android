package com.studyapp.android.ui.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studyapp.android.StudyApp
import com.studyapp.android.data.api.model.repository.Comment
import com.studyapp.android.data.api.model.repository.PostDetail
import com.studyapp.android.data.api.model.repository.PostType
import com.studyapp.android.ui.components.CustomConfirmDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: Long,
    onNavigateBack: () -> Unit,
    viewModel: PostViewModel = viewModel()
) {
    val postDetailState by viewModel.postDetailState.collectAsState()
    var commentText by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ groupId 저장
    var currentGroupId by remember { mutableStateOf<Long?>(null) }
    var isLeader by remember { mutableStateOf(false) }

    LaunchedEffect(postId) {
        viewModel.getPostDetail(postId)
    }

    // ✅ 게시글 로드 시 groupId와 권한 저장
    LaunchedEffect(postDetailState) {
        when (postDetailState) {
            is PostDetailState.Success -> {
                val post = (postDetailState as PostDetailState.Success).post
                currentGroupId = post.groupId
                isLeader = post.isLeader
            }
            is PostDetailState.Deleted -> {
                onNavigateBack()
            }
            is PostDetailState.Error -> {
                // ✅ 에러 메시지 Snackbar 표시
                val errorMessage = (postDetailState as PostDetailState.Error).message
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Short
                )
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) {
                Snackbar(
                    snackbarData = it,
                    containerColor = Color(0xFFFF5252),
                    contentColor = Color.White
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("게시글") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "뒤로가기")
                    }
                },
                actions = {
                    if (postDetailState is PostDetailState.Success) {
                        val post = (postDetailState as PostDetailState.Success).post
                        val currentUserId = StudyApp.currentUserId

                        // ✅ 본인 게시글이거나 그룹장인 경우만 삭제 버튼 표시
                        if (post.userId == currentUserId || isLeader) {
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, "삭제")
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            // 댓글 입력창
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("댓글을 입력하세요") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color(0xFF6B8CFF),
                            unfocusedIndicatorColor = Color(0xFFE0E0E0),
                            cursorColor = Color(0xFF6B8CFF)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                viewModel.createComment(postId, commentText)
                                commentText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6B8CFF)
                        ),
                        enabled = commentText.isNotBlank()
                    ) {
                        Text("등록")
                    }
                }
            }
        }
    ) { paddingValues ->
        when (val state = postDetailState) {
            is PostDetailState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF6B8CFF))
                }
            }
            is PostDetailState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = { viewModel.getPostDetail(postId) }) {
                            Text("다시 시도", color = Color(0xFF6B8CFF))
                        }
                    }
                }
            }
            is PostDetailState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color(0xFFF5F5F5))
                ) {
                    // 게시글 본문
                    item {
                        PostContent(post = state.post)
                    }

                    // 댓글 구분선
                    item {
                        Divider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            thickness = 8.dp,
                            color = Color(0xFFF0F0F0)
                        )
                    }

                    // 댓글 헤더
                    item {
                        Text(
                            text = "댓글 ${state.post.comments.size}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    // 댓글 목록
                    items(state.post.comments) { comment ->
                        CommentItem(
                            comment = comment,
                            isLeader = isLeader,  // ✅ 그룹장 여부 전달
                            onDelete = {
                                // ✅ groupId 전달
                                viewModel.deleteComment(postId, comment.commentId, currentGroupId)
                            }
                        )
                    }

                    // 하단 여백
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            is PostDetailState.Deleted -> {
                // 삭제 완료 - LaunchedEffect에서 처리
            }
        }
    }

    // ✅ 삭제 확인 다이얼로그
    if (showDeleteDialog) {
        CustomConfirmDialog(
            title = "게시글 삭제",
            message = "정말 삭제하시겠습니까?\n삭제된 게시글은 복구할 수 없습니다.",
            confirmText = "삭제",
            dismissText = "취소",
            onConfirm = {
                // ✅ groupId 전달
                viewModel.deletePost(postId, currentGroupId)
                showDeleteDialog = false
            },
            onDismiss = {
                showDeleteDialog = false
            }
        )
    }
}

@Composable
fun PostContent(post: PostDetail) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 타입 뱃지
            if (post.postType == PostType.NOTICE) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFFF5252)
                ) {
                    Text(
                        text = "공지",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 제목
            Text(
                text = post.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 작성자 정보
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.username.first().toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = post.username,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatDateTime(post.createdAt),
                        fontSize = 12.sp,
                        color = Color(0xFF999999)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 내용
            Text(
                text = post.content,
                fontSize = 16.sp,
                color = Color(0xFF333333),
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    isLeader: Boolean,  // ✅ 그룹장 여부
    onDelete: () -> Unit
) {
    val currentUserId = StudyApp.currentUserId
    // ✅ 본인 댓글이거나 그룹장인 경우만 삭제 가능
    val canDelete = comment.userId == currentUserId || isLeader

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = Color.White,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 프로필
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = comment.username.first().toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = comment.username,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatDateTime(comment.createdAt),
                        fontSize = 12.sp,
                        color = Color(0xFF999999)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = comment.content,
                    fontSize = 14.sp,
                    color = Color(0xFF333333)
                )
            }

            // ✅ 삭제 권한이 있을 때만 버튼 표시
            if (canDelete) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "삭제",
                        tint = Color(0xFF999999)
                    )
                }
            }
        }
    }
}

// 날짜 포맷 함수
fun formatDateTime(dateString: String): String {
    return try {
        dateString.substring(0, 10)  // "2025-11-01"
    } catch (e: Exception) {
        dateString
    }
}