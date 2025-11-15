package com.studyapp.android.ui.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studyapp.android.data.api.model.repository.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    onNavigateBack: () -> Unit = {},
    onGroupCreated: () -> Unit = {},
    viewModel: GroupViewModel = viewModel()
) {
    var groupName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var maxMembers by remember { mutableStateOf("") }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // ViewModel State 관찰
    val createGroupState by viewModel.createGroupState.collectAsState()

    // 그룹 생성 상태 처리
    LaunchedEffect(createGroupState) {
        when (createGroupState) {
            is CreateGroupState.Success -> {
                onGroupCreated()
                viewModel.resetCreateGroupState()
            }
            is CreateGroupState.Error -> {
                errorMessage = (createGroupState as CreateGroupState.Error).message
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 바
        TopAppBar(
            title = {
                Text(
                    text = "스터디 그룹 만들기",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // 그룹명
            Text(
                text = "그룹명",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("그룹 이름을 입력하세요") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color(0xFF6B8CFF),
                    unfocusedIndicatorColor = Color(0xFFE0E0E0),
                    cursorColor = Color(0xFF6B8CFF),
                    focusedLabelColor = Color(0xFF6B8CFF)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 설명
            Text(
                text = "설명",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text("그룹 설명을 입력하세요") },
                maxLines = 5,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color(0xFF6B8CFF),
                    unfocusedIndicatorColor = Color(0xFFE0E0E0),
                    cursorColor = Color(0xFF6B8CFF),
                    focusedLabelColor = Color(0xFF6B8CFF)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 카테고리
            Text(
                text = "카테고리",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = selectedCategory?.displayName ?: "",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCategoryDialog = true },
                placeholder = { Text("카테고리 선택") },
                enabled = false,
                trailingIcon = {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                },
                colors = TextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledContainerColor = Color.Transparent,
                    disabledIndicatorColor = Color(0xFFE0E0E0),
                    disabledPlaceholderColor = Color(0xFFCCCCCC)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 최대 인원
            Text(
                text = "최대 인원",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = maxMembers,
                onValueChange = {
                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                        maxMembers = it
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("최대 인원 수를 입력하세요") },
                singleLine = true,
                suffix = { Text("명") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color(0xFF6B8CFF),
                    unfocusedIndicatorColor = Color(0xFFE0E0E0),
                    cursorColor = Color(0xFF6B8CFF),
                    focusedLabelColor = Color(0xFF6B8CFF)
                )
            )

            // 에러 메시지
            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage,
                    color = Color(0xFFFF5252),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 생성 버튼
            Button(
                onClick = {
                    if (groupName.isEmpty()) {
                        errorMessage = "그룹명을 입력하세요"
                    } else if (selectedCategory == null) {
                        errorMessage = "카테고리를 선택하세요"
                    } else if (maxMembers.isEmpty() || maxMembers.toIntOrNull() == null || maxMembers.toInt() < 2) {
                        errorMessage = "최대 인원을 2명 이상으로 입력하세요"
                    } else {
                        errorMessage = ""
                        viewModel.createGroup(
                            groupName = groupName,
                            description = description,
                            category = selectedCategory!!,
                            maxMembers = maxMembers.toInt()
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6B8CFF)
                ),
                enabled = createGroupState !is CreateGroupState.Loading
            ) {
                if (createGroupState is CreateGroupState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "그룹 만들기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // 카테고리 선택 다이얼로그
    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("카테고리 선택") },
            text = {
                Column {
                    Category.values().forEach { category ->
                        TextButton(
                            onClick = {
                                selectedCategory = category
                                showCategoryDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = category.displayName,
                                modifier = Modifier.fillMaxWidth(),
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("취소", color = Color(0xFF6B8CFF))
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CreateGroupScreenPreview() {
    CreateGroupScreen()
}