package com.studyapp.android.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studyapp.android.ui.auth.CustomTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = viewModel()
) {
    var nickname by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val updateState by viewModel.updateState.collectAsState()
    val profileState by viewModel.profileState.collectAsState()

    // 프로필 로드
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    // 프로필 로드 완료 시 초기값 설정
    LaunchedEffect(profileState) {
        if (profileState is EditProfileState.Success) {
            val profile = (profileState as EditProfileState.Success).profile
            nickname = profile.nickname
            bio = profile.bio ?: ""
            isLoading = false
        }
    }

    // 수정 완료 시 뒤로가기
    LaunchedEffect(updateState) {
        if (updateState is UpdateProfileState.Success) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "프로필 수정",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "뒤로가기")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (nickname.isNotBlank()) {
                                viewModel.updateProfile(nickname, bio.ifBlank { null })
                            }
                        },
                        enabled = nickname.isNotBlank() && updateState !is UpdateProfileState.Loading
                    ) {
                        Text(
                            "완료",
                            color = if (nickname.isNotBlank()) Color(0xFF6B8CFF) else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF6B8CFF))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // 닉네임 입력
                CustomTextField(
                    label = "닉네임",
                    value = nickname,
                    onValueChange = {
                        if (it.length <= 20) {
                            nickname = it
                        }
                    },
                    placeholder = "닉네임을 입력하세요"
                )

                // 글자 수 제한 표시
                Text(
                    text = "${nickname.length}/20",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 소개글 입력
                CustomTextField(
                    label = "소개글",
                    value = bio,
                    onValueChange = {
                        if (it.length <= 100) {
                            bio = it
                        }
                    },
                    placeholder = "자기소개를 입력하세요 (선택)"
                )

                // 글자 수 제한 표시
                Text(
                    text = "${bio.length}/100",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 안내 문구
                Text(
                    text = "※ 소개글은 다른 사용자에게 표시됩니다",
                    fontSize = 13.sp,
                    color = Color(0xFF999999)
                )

                // 에러 메시지
                if (updateState is UpdateProfileState.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFEBEE)
                    ) {
                        Text(
                            text = (updateState as UpdateProfileState.Error).message,
                            color = Color(0xFFFF5252),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 저장 버튼
                Button(
                    onClick = {
                        if (nickname.isNotBlank()) {
                            viewModel.updateProfile(nickname, bio.ifBlank { null })
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6B8CFF)
                    ),
                    enabled = nickname.isNotBlank() && updateState !is UpdateProfileState.Loading
                ) {
                    if (updateState is UpdateProfileState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "저장",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}