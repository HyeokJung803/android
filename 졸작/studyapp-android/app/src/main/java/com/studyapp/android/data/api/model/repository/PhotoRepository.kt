package com.studyapp.android.data.api.model.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class PhotoRepository {

    private val apiService = RetrofitClient.apiService

    // 그룹 사진 목록 조회
    suspend fun getGroupPhotos(groupId: Long): Result<List<Photo>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getGroupPhotos(groupId)

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("사진 조회 실패"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // 사진 업로드
    suspend fun uploadPhoto(
        groupId: Long,
        userId: Long,
        imageFile: File,
        description: String?
    ): Result<Photo> {
        return withContext(Dispatchers.IO) {
            try {
                // 이미지 파일을 MultipartBody.Part로 변환
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

                // description을 RequestBody로 변환
                val descriptionBody = description?.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = apiService.uploadPhoto(
                    groupId,
                    userId,
                    imagePart,
                    descriptionBody
                )

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("사진 업로드 실패"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ✅ 사진 삭제 (groupId 전달)
    suspend fun deletePhoto(
        photoId: Long,
        userId: Long,
        groupId: Long?
    ): Result<ApiResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // ✅ groupId 파라미터 추가
                val response = apiService.deletePhoto(photoId, userId, groupId)

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception(errorBody ?: "사진 삭제 실패"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}