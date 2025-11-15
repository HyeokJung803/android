package com.studyapp.android.ui.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studyapp.android.data.api.model.repository.PostType
import com.studyapp.android.ui.components.CustomSelectDialog
import com.studyapp.android.ui.components.SelectOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    groupId: Long,
    onNavigateBack: () -> Unit,
    viewModel: PostViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedPostType by remember { mutableStateOf(PostType.FREE) }
    var showTypeDialog by remember { mutableStateOf(false) }

    val createPostState by viewModel.createPostState.collectAsState()

    LaunchedEffect(createPostState) {
        if (createPostState is CreatePostState.Success) {
            onNavigateBack()
            viewModel.resetCreatePostState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("게시글 작성") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "뒤로가기")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (title.isNotBlank() && content.isNotBlank()) {
                                viewModel.createPost(groupId, title, content, selectedPostType)
                            }
                        },
                        enabled = title.isNotBlank() && content.isNotBlank()
                    ) {
                        Text(
                            "완료",
                            color = if (title.isNotBlank() && content.isNotBlank())
                                Color(0xFF6B8CFF)
                            else
                                Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(16.dp)
        ) {
            // 게시글 타입 선택 - 커스텀 버튼으로 변경!
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF5F5F5),
                onClick = { showTypeDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "게시글 유형",
                            fontSize = 13.sp,
                            color = Color(0xFF999999)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedPostType.displayName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color(0xFF666666)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 제목 입력
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("제목을 입력하세요") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color(0xFF6B8CFF),
                    unfocusedIndicatorColor = Color(0xFFE0E0E0)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 내용 입력
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { Text("내용을 입력하세요") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color(0xFF6B8CFF),
                    unfocusedIndicatorColor = Color(0xFFE0E0E0)
                )
            )

            if (createPostState is CreatePostState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF6B8CFF))
                }
            }

            if (createPostState is CreatePostState.Error) {
                Text(
                    text = (createPostState as CreatePostState.Error).message,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }

    // 게시글 타입 선택 다이얼로그 - 커스텀으로 변경!
    if (showTypeDialog) {
        val options = listOf(
            SelectOption(PostType.FREE.name, "자유 게시글"),
            SelectOption(PostType.NOTICE.name, "공지사항")
        )

        CustomSelectDialog(
            title = "게시글 유형 선택",
            options = options,
            selectedOption = SelectOption(selectedPostType.name, selectedPostType.displayName),
            onOptionSelected = { selected ->
                selectedPostType = PostType.valueOf(selected.value)
                showTypeDialog = false
            },
            onDismiss = { showTypeDialog = false }
        )
    }
}