package com.studyapp.android.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyapp.android.data.api.model.repository.AuthRepository
import com.studyapp.android.data.api.model.repository.AuthResponse
import com.studyapp.android.data.api.model.repository.LoginRequest
import com.studyapp.android.data.api.model.repository.SignupRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _signupState = MutableStateFlow<AuthState>(AuthState.Idle)
    val signupState: StateFlow<AuthState> = _signupState

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState

    private val _nicknameCheckState = MutableStateFlow<NicknameCheckState>(NicknameCheckState.Idle)
    val nicknameCheckState: StateFlow<NicknameCheckState> = _nicknameCheckState


    var currentUser: AuthResponse? = null
        private set

    fun signup(email: String, password: String, name: String, nickname: String, birthDate: String) {
        viewModelScope.launch {
            _signupState.value = AuthState.Loading
            val request = SignupRequest(email, password, name, nickname, birthDate)
            val result = repository.signup(request)

            _signupState.value = if (result.isSuccess) {
                val response = result.getOrNull()!!
                if (response.success) {
                    AuthState.Success(response.message)
                } else {
                    AuthState.Error(response.message)
                }
            } else {
                AuthState.Error("네트워크 오류가 발생했습니다.")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = AuthState.Loading
            val request = LoginRequest(email, password)
            val result = repository.login(request)

            _loginState.value = if (result.isSuccess) {
                val response = result.getOrNull()!!
                if (response.success) {
                    currentUser = response
                    AuthState.Success(response.message)
                } else {
                    AuthState.Error(response.message)
                }
            } else {
                AuthState.Error("네트워크 오류가 발생했습니다.")
            }
        }
    }

    fun checkNickname(nickname: String) {
        viewModelScope.launch {
            _nicknameCheckState.value = NicknameCheckState.Loading
            val result = repository.checkNickname(nickname)

            _nicknameCheckState.value = if (result.isSuccess) {
                val response = result.getOrNull()!!
                if (response.success) {
                    NicknameCheckState.Available(response.message)
                } else {
                    NicknameCheckState.Unavailable(response.message)
                }
            } else {
                NicknameCheckState.Error("닉네임 확인 중 오류가 발생했습니다.")
            }
        }
    }

    fun resetSignupState() {
        _signupState.value = AuthState.Idle
    }

    fun resetLoginState() {
        _loginState.value = AuthState.Idle
        currentUser = null
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class NicknameCheckState {
    object Idle : NicknameCheckState()
    object Loading : NicknameCheckState()
    data class Available(val message: String) : NicknameCheckState()
    data class Unavailable(val message: String) : NicknameCheckState()
    data class Error(val message: String) : NicknameCheckState()
}