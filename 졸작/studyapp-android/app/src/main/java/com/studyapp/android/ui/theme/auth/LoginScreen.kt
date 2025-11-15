package com.studyapp.android.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studyapp.android.StudyApp
import com.studyapp.android.util.PreferenceManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onNavigateToSignup: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // ViewModel State 관찰
    val loginState by viewModel.loginState.collectAsState()

    // 로그인 상태 처리
    // 로그인 상태 처리
    LaunchedEffect(loginState) {
        when (loginState) {
            is AuthState.Success -> {
                viewModel.currentUser?.let { response ->
                    val userId = response.userId
                    val nickname = response.nickname

                    // ✅ 검증: userId가 정상적인지 확인
                    if (userId != null && userId > 0 && !nickname.isNullOrEmpty()) {
                        // 전역 변수에 저장
                        StudyApp.currentUserId = userId
                        StudyApp.currentNickname = nickname

                        // SharedPreferences에 영구 저장
                        PreferenceManager.saveUser(context, userId, nickname)

                        println("🟢 로그인 성공! userId=$userId, nickname=$nickname")

                        onLoginSuccess()
                    } else {
                        // ✅ 비정상적인 응답 처리
                        println("🔴 로그인 응답 오류: userId=$userId, nickname=$nickname")
                        // TODO: 사용자에게 오류 메시지 표시
                    }
                }

                viewModel.resetLoginState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 로고 또는 앱 이름
        Text(
            text = "StudyApp",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B8CFF)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "함께 공부하는 즐거움",
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(64.dp))

        // 이메일 입력
        CustomTextField(
            label = "이메일",
            value = email,
            onValueChange = { email = it },
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 비밀번호 입력
        CustomTextField(
            label = "비밀번호",
            value = password,
            onValueChange = { password = it },
            keyboardType = KeyboardType.Password,
            isPassword = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 에러 메시지
        if (loginState is AuthState.Error) {
            Text(
                text = (loginState as AuthState.Error).message,
                color = Color(0xFFFF5252),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 로그인 버튼
        Button(
            onClick = {
                viewModel.login(email, password)  // ViewModel 호출!
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6B8CFF),
                contentColor = Color.White
            ),
            enabled = loginState !is AuthState.Loading && email.isNotEmpty() && password.isNotEmpty()
        ) {
            if (loginState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text(
                    text = "로그인",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 회원가입 이동
        TextButton(
            onClick = onNavigateToSignup,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "계정이 없으신가요? 회원가입",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}