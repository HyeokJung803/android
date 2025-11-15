package com.studyapp.android.ui.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studyapp.android.data.api.model.repository.Post
import com.studyapp.android.data.api.model.repository.PostType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListScreen(
    groupId: Long,
    onPostClick: (Long) -> Unit,
    onCreatePostClick: () -> Unit,
    viewModel: PostViewModel = viewModel()
) {
    var selectedFilter by remember { mutableStateOf<PostType?>(null) }
    val postListState by viewModel.postListState.collectAsState()

    LaunchedEffect(groupId, selectedFilter) {
        viewModel.getPosts(groupId, selectedFilter)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            // 필터 탭
            TabRow(
                selectedTabIndex = when (selectedFilter) {
                    null -> 0
                    PostType.NOTICE -> 1
                    PostType.FREE -> 2
                },
                containerColor = Color.White,
                contentColor = Color.Black,
                modifier = Modifier.fillMaxWidth(),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[when (selectedFilter) {
                            null -> 0
                            PostType.NOTICE -> 1
                            PostType.FREE -> 2
                        }]),
                        color = Color(0xFF6B8CFF)
                    )
                }
            ) {
                Tab(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    text = {
                        Text(
                            "전체",
                            fontWeight = if (selectedFilter == null) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedFilter == null) Color(0xFF6B8CFF) else Color.Black
                        )
                    }
                )
                Tab(
                    selected = selectedFilter == PostType.NOTICE,
                    onClick = { selectedFilter = PostType.NOTICE },
                    text = {
                        Text(
                            "공지",
                            fontWeight = if (selectedFilter == PostType.NOTICE) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedFilter == PostType.NOTICE) Color(0xFF6B8CFF) else Color.Black
                        )
                    }
                )
                Tab(
                    selected = selectedFilter == PostType.FREE,
                    onClick = { selectedFilter = PostType.FREE },
                    text = {
                        Text(
                            "자유",
                            fontWeight = if (selectedFilter == PostType.FREE) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedFilter == PostType.FREE) Color(0xFF6B8CFF) else Color.Black
                        )
                    }
                )
            }

            // 게시글 목록
            when (val state = postListState) {
                is PostListState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF6B8CFF))
                    }
                }
                is PostListState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(state.message, color = Color.Gray)
                    }
                }
                is PostListState.Success -> {
                    if (state.posts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "작성된 게시글이 없습니다",
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "첫 게시글을 작성해보세요!",
                                    fontSize = 14.sp,
                                    color = Color(0xFF999999)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 0.dp,
                                end = 0.dp,
                                top = 8.dp,
                                bottom = 88.dp  // FAB 공간 확보
                            )
                        ) {
                            items(state.posts) { post ->
                                PostItem(
                                    post = post,
                                    onClick = { onPostClick(post.postId) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // FAB을 Box의 자식으로 배치
        FloatingActionButton(
            onClick = onCreatePostClick,
            containerColor = Color(0xFF6B8CFF),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)  // 우하단에서 16dp 떨어짐
        ) {
            Icon(Icons.Default.Add, "게시글 작성", tint = Color.White)
        }
    }
}

@Composable
fun PostItem(
    post: Post,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 헤더 (타입 뱃지 + 작성자)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 게시글 타입 뱃지
                    if (post.postType == PostType.NOTICE) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFF5252)
                        ) {
                            Text(
                                text = "공지",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = post.username,
                        fontSize = 13.sp,
                        color = Color(0xFF666666)
                    )
                }

                Text(
                    text = formatDate(post.createdAt),
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 제목
            Text(
                text = post.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 내용 미리보기
            Text(
                text = post.content,
                fontSize = 14.sp,
                color = Color(0xFF666666),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 댓글 수
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "💬 댓글 ${post.commentCount}",
                    fontSize = 13.sp,
                    color = Color(0xFF6B8CFF)
                )
            }
        }
    }
}

// 날짜 포맷 함수
fun formatDate(dateString: String): String {
    // 간단한 날짜 포맷 (실제로는 SimpleDateFormat 사용)
    return try {
        dateString.substring(0, 10)  // "2025-11-01"
    } catch (e: Exception) {
        dateString
    }
}