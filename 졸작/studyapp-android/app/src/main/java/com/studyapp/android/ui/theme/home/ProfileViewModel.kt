package com.studyapp.android.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyapp.android.StudyApp
import com.studyapp.android.data.api.model.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val repository = UserRepository()

    private val currentUserId: Long
        get() = StudyApp.currentUserId

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading

            val result = repository.getUserProfile(currentUserId)

            _profileState.value = if (result.isSuccess) {
                ProfileState.Success(result.getOrNull()!!)
            } else {
                ProfileState.Error(result.exceptionOrNull()?.message ?: "프로필 로드 실패")
            }
        }
    }
}

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val profile: UserProfileData) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

data class UserProfileData(
    val userId: Long,
    val email: String,
    val name: String,
    val nickname: String,
    val birthDate: String,
    val profileImage: String?,
    val bio: String?,
    val createdAt: String,
    val postCount: Int,
    val commentCount: Int,
    val photoCount: Int,
    val groupCount: Int
)