package com.studyapp.android.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyapp.android.StudyApp
import com.studyapp.android.data.api.model.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChangePasswordViewModel : ViewModel() {
    private val repository = UserRepository()

    private val currentUserId: Long
        get() = StudyApp.currentUserId

    private val _changePasswordState = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
    val changePasswordState: StateFlow<ChangePasswordState> = _changePasswordState

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _changePasswordState.value = ChangePasswordState.Loading

            val result = repository.changePassword(currentUserId, currentPassword, newPassword)

            _changePasswordState.value = if (result.isSuccess) {
                val response = result.getOrNull()!!
                if (response.success) {
                    ChangePasswordState.Success(response.message)
                } else {
                    ChangePasswordState.Error(response.message)
                }
            } else {
                ChangePasswordState.Error(result.exceptionOrNull()?.message ?: "비밀번호 변경 실패")
            }
        }
    }
}

sealed class ChangePasswordState {
    object Idle : ChangePasswordState()
    object Loading : ChangePasswordState()
    data class Success(val message: String) : ChangePasswordState()
    data class Error(val message: String) : ChangePasswordState()
}