package com.studyapp.android.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyapp.android.StudyApp
import com.studyapp.android.data.api.model.repository.UserRepository
import com.studyapp.android.data.api.model.repository.UserStatsResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EditProfileViewModel : ViewModel() {
    private val repository = UserRepository()

    private val currentUserId: Long
        get() = StudyApp.currentUserId

    private val _profileState = MutableStateFlow<EditProfileState>(EditProfileState.Loading)
    val profileState: StateFlow<EditProfileState> = _profileState

    private val _updateState = MutableStateFlow<UpdateProfileState>(UpdateProfileState.Idle)
    val updateState: StateFlow<UpdateProfileState> = _updateState

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = EditProfileState.Loading

            val result = repository.getUserProfile(currentUserId)

            _profileState.value = if (result.isSuccess) {
                EditProfileState.Success(result.getOrNull()!!)
            } else {
                EditProfileState.Error(result.exceptionOrNull()?.message ?: "프로필 로드 실패")
            }
        }
    }

    fun updateProfile(nickname: String, bio: String?) {
        viewModelScope.launch {
            _updateState.value = UpdateProfileState.Loading

            val result = repository.updateProfile(currentUserId, nickname, bio)

            _updateState.value = if (result.isSuccess) {
                val response = result.getOrNull()!!
                if (response.success) {
                    // 전역 닉네임 업데이트
                    StudyApp.currentNickname = nickname
                    UpdateProfileState.Success(response.message)
                } else {
                    UpdateProfileState.Error(response.message)
                }
            } else {
                UpdateProfileState.Error(result.exceptionOrNull()?.message ?: "프로필 수정 실패")
            }
        }
    }
}

sealed class EditProfileState {
    object Loading : EditProfileState()
    data class Success(val profile: UserProfileData) : EditProfileState()
    data class Error(val message: String) : EditProfileState()
}

sealed class UpdateProfileState {
    object Idle : UpdateProfileState()
    object Loading : UpdateProfileState()
    data class Success(val message: String) : UpdateProfileState()
    data class Error(val message: String) : UpdateProfileState()
}