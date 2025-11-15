package com.studyapp.android.ui.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyapp.android.StudyApp
import com.studyapp.android.data.api.model.repository.Photo
import com.studyapp.android.data.api.model.repository.PhotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class PhotoViewModel : ViewModel() {
    private val repository = PhotoRepository()
    private val currentUserId: Long
        get() = StudyApp.currentUserId

    private val _photoListState = MutableStateFlow<PhotoListState>(PhotoListState.Loading)
    val photoListState: StateFlow<PhotoListState> = _photoListState

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState

    // 그룹 사진 목록 조회
    fun getGroupPhotos(groupId: Long) {
        viewModelScope.launch {
            _photoListState.value = PhotoListState.Loading
            val result = repository.getGroupPhotos(groupId)

            _photoListState.value = if (result.isSuccess) {
                PhotoListState.Success(result.getOrNull() ?: emptyList())
            } else {
                PhotoListState.Error(result.exceptionOrNull()?.message ?: "사진 조회 실패")
            }
        }
    }

    // 사진 업로드
    fun uploadPhoto(groupId: Long, imageFile: File, description: String?) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Loading

            val result = repository.uploadPhoto(groupId, currentUserId, imageFile, description)

            _uploadState.value = if (result.isSuccess) {
                // 업로드 성공 후 목록 새로고침
                getGroupPhotos(groupId)
                UploadState.Success("사진이 업로드되었습니다")
            } else {
                UploadState.Error(result.exceptionOrNull()?.message ?: "사진 업로드 실패")
            }
        }
    }

    // ✅ 사진 삭제 (groupId 전달)
    fun deletePhoto(groupId: Long, photoId: Long) {
        viewModelScope.launch {
            // ✅ groupId 전달
            val result = repository.deletePhoto(photoId, currentUserId, groupId)

            if (result.isSuccess) {
                // 삭제 성공 후 목록 새로고침
                getGroupPhotos(groupId)
            } else {
                _photoListState.value = PhotoListState.Error(
                    result.exceptionOrNull()?.message ?: "사진 삭제 실패"
                )
            }
        }
    }

    fun resetUploadState() {
        _uploadState.value = UploadState.Idle
    }
}

// State 정의
sealed class PhotoListState {
    object Loading : PhotoListState()
    data class Success(val photos: List<Photo>) : PhotoListState()
    data class Error(val message: String) : PhotoListState()
}

sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    data class Success(val message: String) : UploadState()
    data class Error(val message: String) : UploadState()
}