package com.studyapp.android.ui.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.studyapp.android.data.api.model.repository.GroupDetailResponse
import com.studyapp.android.data.api.model.repository.GroupMemberDetail
import com.studyapp.android.ui.components.CustomConfirmDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: Long,
    onNavigateBack: () -> Unit = {},
    viewModel: GroupViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("홈", "게시판", "사진첩", "채팅")
    var showJoinDialog by remember { mutableStateOf(false) }
    var selectedPostId by remember { mutableStateOf<Long?>(null) }
    var showCreatePost by remember { mutableStateOf(false) }
    var memberToKick by remember { mutableStateOf<GroupMemberDetail?>(null) }
    var showKickDialog by remember { mutableStateOf(false) }

    val groupDetailState by viewModel.groupDetailState.collectAsState()

    LaunchedEffect(groupId) {
        viewModel.getGroupDetail(groupId)
    }

    // 게시글 상세 화면
    if (selectedPostId != null) {
        PostDetailScreen(
            postId = selectedPostId!!,
            onNavigateBack = { selectedPostId = null }
        )
        return
    }

    // 게시글 작성 화면
    if (showCreatePost) {
        CreatePostScreen(
            groupId = groupId,
            onNavigateBack = { showCreatePost = false }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val state = groupDetailState) {
                        is GroupDetailState.Success -> {
                            Text(
                                text = state.detail.groupName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                maxLines = 1
                            )
                        }
                        else -> {
                            Text(
                                text = "그룹 상세",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        bottomBar = {
            if (groupDetailState is GroupDetailState.Success && selectedTab == 0) {
                val detail = (groupDetailState as GroupDetailState.Success).detail
                if (!detail.isJoined) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shadowElevation = 8.dp
                    ) {
                        Button(
                            onClick = { showJoinDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6B8CFF)
                            )
                        ) {
                            Text(
                                text = "가입하기",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        when (val state = groupDetailState) {
            is GroupDetailState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF6B8CFF))
                }
            }
            is GroupDetailState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = state.message, color = Color.Gray, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = { viewModel.getGroupDetail(groupId) }) {
                            Text("다시 시도", color = Color(0xFF6B8CFF))
                        }
                    }
                }
            }
            is GroupDetailState.Success -> {
                val detail = state.detail
                val isLeader = detail.members.any {
                    it.isLeader && it.userId == StudyApp.currentUserId
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color.White)
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = Color(0xFF6B8CFF)
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 15.sp,
                                        color = if (selectedTab == index) Color(0xFF6B8CFF) else Color.Black
                                    )
                                }
                            )
                        }
                    }

                    when (selectedTab) {
                        0 -> HomeTab(
                            groupDetail = detail,
                            isLeader = isLeader,
                            onKickClick = { member ->
                                memberToKick = member
                                showKickDialog = true
                            }
                        )
                        1 -> PostListScreen(
                            groupId = groupId,
                            onPostClick = { postId -> selectedPostId = postId },
                            onCreatePostClick = { showCreatePost = true }
                        )
                        2 -> PhotoGalleryScreen(
                            groupId = groupId,
                            isLeader = isLeader
                        )
                        3 -> ChatScreen(groupId = groupId)
                    }
                }
            }
        }
    }

    // 가입 다이얼로그
    if (showJoinDialog) {
        JoinGroupDialog(
            onDismiss = { showJoinDialog = false },
            onConfirm = { greeting ->
                viewModel.joinGroupWithGreeting(groupId, greeting)
                showJoinDialog = false
            }
        )
    }

    // 강퇴 확인 다이얼로그
    if (showKickDialog && memberToKick != null) {
        CustomConfirmDialog(
            title = "멤버 강퇴",
            message = "${memberToKick!!.username}님을 정말 강퇴하시겠습니까?",
            confirmText = "강퇴",
            dismissText = "취소",
            onConfirm = {
                viewModel.kickMember(groupId, memberToKick!!.userId)
                showKickDialog = false
                memberToKick = null
            },
            onDismiss = {
                showKickDialog = false
                memberToKick = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var greetingText by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = "가입인사를 작성해주세요",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = greetingText,
                    onValueChange = {
                        greetingText = it
                        showError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("가입인사를 입력해주세요") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color(0xFF6B8CFF),
                        unfocusedIndicatorColor = Color(0xFFE0E0E0)
                    ),
                    isError = showError
                )
                if (showError) {
                    Text(
                        text = "가입인사를 작성해주세요",
                        fontSize = 12.sp,
                        color = Color(0xFFFF5252),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (greetingText.trim().isEmpty()) {
                        showError = true
                    } else {
                        onConfirm(greetingText)
                    }
                }
            ) {
                Text("확인", color = Color(0xFF6B8CFF), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = Color.Gray)
            }
        }
    )
}

@Composable
fun HomeTab(
    groupDetail: GroupDetailResponse,
    isLeader: Boolean,
    onKickClick: (GroupMemberDetail) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF6B8CFF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = groupDetail.groupName.take(10).uppercase(),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Chip(groupDetail.category.displayName)
                    Chip("멤버 ${groupDetail.currentMembers}/${groupDetail.maxMembers}")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = groupDetail.groupName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = groupDetail.description,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    lineHeight = 22.sp
                )
            }
        }

        item {
            Text(
                text = "모임 멤버 (${groupDetail.currentMembers})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }

        items(groupDetail.members) { member ->
            MemberCard(
                member = member,
                isLeader = isLeader,
                canKick = member.userId != StudyApp.currentUserId,
                onKickClick = { onKickClick(member) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MemberCard(
    member: GroupMemberDetail,
    isLeader: Boolean,
    canKick: Boolean,
    onKickClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (member.isLeader) Color(0xFFFFD700)
                    else Color(0xFFE0E0E0)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = member.username?.firstOrNull()?.toString() ?: "?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = member.username ?: "이름 없음",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )

                if (member.isLeader) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFFFF3E0)
                    ) {
                        Text(
                            text = "그룹장",
                            fontSize = 11.sp,
                            color = Color(0xFFFFB74D),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (member.isNew) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "NEW",
                        fontSize = 11.sp,
                        color = Color(0xFFFF5252),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 강퇴 버튼 (그룹장이고, 대상이 그룹장 아니고, 본인 아닐 때)
        if (isLeader && !member.isLeader && canKick) {
            IconButton(onClick = onKickClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "강퇴",
                    tint = Color(0xFFFF5252)
                )
            }
        }
    }
}

@Composable
fun Chip(text: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}