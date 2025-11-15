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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studyapp.android.ui.auth.CustomTextField
import com.studyapp.android.ui.auth.PasswordRequirement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChangePasswordViewModel = viewModel()
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val changePasswordState by viewModel.changePasswordState.collectAsState()

    // 비밀번호 변경 완료 시 뒤로가기
    LaunchedEffect(changePasswordState) {
        if (changePasswordState is ChangePasswordState.Success) {
            onNavigateBack()
        }
    }

    // 비밀번호 유효성 검사
    val isPasswordValid = newPassword.length in 8..19 &&
            newPassword.any { it.isLetter() } &&
            newPassword.any { it.isDigit() } &&
            !newPassword.any { !it.isLetterOrDigit() }

    val isPasswordMatch = newPassword.isNotEmpty() && newPassword == confirmPassword

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "비밀번호 변경",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "뒤로가기")
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 안내 문구
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF5F7FA)
            ) {
                Text(
                    text = "보안을 위해 주기적으로 비밀번호를 변경해주세요.",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 현재 비밀번호
            CustomTextField(
                label = "현재 비밀번호",
                value = currentPassword,
                onValueChange = { currentPassword = it },
                keyboardType = KeyboardType.Password,
                isPassword = true,
                placeholder = "현재 비밀번호를 입력하세요"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 새 비밀번호
            CustomTextField(
                label = "새 비밀번호",
                value = newPassword,
                onValueChange = { newPassword = it },
                keyboardType = KeyboardType.Password,
                isPassword = true,
                placeholder = "새 비밀번호를 입력하세요"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 비밀번호 조건
            PasswordRequirement(
                "영문, 숫자 조합을 사용해주세요",
                newPassword.any { it.isLetter() } && newPassword.any { it.isDigit() }
            )
            Spacer(modifier = Modifier.height(8.dp))
            PasswordRequirement(
                "최소 8자리 이상, 20자 미만으로 구성해주세요",
                newPassword.length in 8..19
            )
            Spacer(modifier = Modifier.height(8.dp))
            PasswordRequirement(
                "특수문자는 사용할 수 없어요",
                newPassword.isEmpty() || !newPassword.any { !it.isLetterOrDigit() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 비밀번호 확인
            CustomTextField(
                label = "비밀번호 확인",
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                keyboardType = KeyboardType.Password,
                isPassword = true,
                placeholder = "비밀번호를 다시 입력하세요"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 비밀번호 일치 여부
            if (confirmPassword.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "• ",
                        color = if (isPasswordMatch) Color(0xFF4CAF50) else Color(0xFFFF5252),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isPasswordMatch) "비밀번호가 일치합니다" else "비밀번호가 일치하지 않습니다",
                        color = if (isPasswordMatch) Color(0xFF4CAF50) else Color(0xFFFF5252),
                        fontSize = 13.sp
                    )
                }
            }

            // 에러 메시지
            if (changePasswordState is ChangePasswordState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFEBEE)
                ) {
                    Text(
                        text = (changePasswordState as ChangePasswordState.Error).message,
                        color = Color(0xFFFF5252),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 변경 버튼
            Button(
                onClick = {
                    if (currentPassword.isNotEmpty() && isPasswordValid && isPasswordMatch) {
                        viewModel.changePassword(currentPassword, newPassword)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6B8CFF)
                ),
                enabled = currentPassword.isNotEmpty() &&
                        isPasswordValid &&
                        isPasswordMatch &&
                        changePasswordState !is ChangePasswordState.Loading
            ) {
                if (changePasswordState is ChangePasswordState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "비밀번호 변경",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}