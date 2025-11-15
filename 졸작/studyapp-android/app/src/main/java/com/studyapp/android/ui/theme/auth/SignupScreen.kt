package com.studyapp.android.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()  // ViewModel 추가!
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }

    // ViewModel State 관찰
    val signupState by viewModel.signupState.collectAsState()
    val nicknameCheckState by viewModel.nicknameCheckState.collectAsState()

    // 회원가입 상태 처리
    LaunchedEffect(signupState) {
        when (signupState) {
            is AuthState.Success -> {
                onSignupSuccess()
                viewModel.resetSignupState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // 뒤로가기 버튼
        IconButton(
            onClick = onNavigateToLogin,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "뒤로가기",
                tint = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 타이틀
        Text(
            text = "회원가입",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(48.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        // 비밀번호 조건
        PasswordRequirement("영문, 숫자 조합을 사용해주세요", password.any { it.isLetter() } && password.any { it.isDigit() })
        Spacer(modifier = Modifier.height(8.dp))
        PasswordRequirement("최소 8자리 이상, 20자 미만으로 구성해주세요", password.length in 8..19)
        Spacer(modifier = Modifier.height(8.dp))
        PasswordRequirement("특수문자는 사용할 수 없어요", password.isEmpty() || !password.any { !it.isLetterOrDigit() })

        Spacer(modifier = Modifier.height(24.dp))

        // 이름 입력
        CustomTextField(
            label = "이름",
            value = name,
            onValueChange = { name = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 닉네임 입력 + 중복확인
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                CustomTextField(
                    label = "닉네임 (다른 사용자에게 보여집니다)",
                    value = nickname,
                    onValueChange = { nickname = it }
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    viewModel.checkNickname(nickname)  // ViewModel 호출!
                },
                modifier = Modifier
                    .height(56.dp)
                    .padding(bottom = 0.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6B8CFF)
                ),
                enabled = nickname.isNotEmpty() && nicknameCheckState !is NicknameCheckState.Loading
            ) {
                if (nicknameCheckState is NicknameCheckState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text("중복확인", fontSize = 14.sp)
                }
            }
        }

        // 닉네임 체크 메시지
        when (nicknameCheckState) {
            is NicknameCheckState.Available -> {
                Text(
                    text = (nicknameCheckState as NicknameCheckState.Available).message,
                    color = Color(0xFF4CAF50),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            is NicknameCheckState.Unavailable -> {
                Text(
                    text = (nicknameCheckState as NicknameCheckState.Unavailable).message,
                    color = Color(0xFFFF5252),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            is NicknameCheckState.Error -> {
                Text(
                    text = (nicknameCheckState as NicknameCheckState.Error).message,
                    color = Color(0xFFFF5252),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 생년월일 입력
        CustomTextField(
            label = "생년월일 (YYYY-MM-DD)",
            value = birthDate,
            onValueChange = { birthDate = it },
            placeholder = "2000-01-01"
        )

        // 에러 메시지
        if (signupState is AuthState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (signupState as AuthState.Error).message,
                color = Color(0xFFFF5252),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 회원가입 버튼
        Button(
            onClick = {
                viewModel.signup(email, password, name, nickname, birthDate)  // ViewModel 호출!
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6B8CFF),
                contentColor = Color.White
            ),
            enabled = signupState !is AuthState.Loading &&
                    email.isNotEmpty() &&
                    password.isNotEmpty() &&
                    name.isNotEmpty() &&
                    nickname.isNotEmpty() &&
                    birthDate.isNotEmpty()
        ) {
            if (signupState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text(
                    text = "회원가입",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 로그인 이동
        TextButton(
            onClick = onNavigateToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "이미 계정이 있으신가요? 로그인",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    placeholder: String = ""
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF666666),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Black,
                unfocusedIndicatorColor = Color(0xFFE0E0E0),
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
            ),
            placeholder = {
                if (placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        color = Color(0xFFCCCCCC),
                        fontSize = 16.sp
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
        )
    }
}

@Composable
fun PasswordRequirement(text: String, isValid: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "• ",
            color = if (isValid) Color(0xFF4CAF50) else Color(0xFFFF5252),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = text,
            color = Color(0xFF666666),
            fontSize = 13.sp
        )
    }
}
@Preview(showBackground = true)
@Composable
fun SignupScreenPreview() {
    SignupScreen()
}