package com.studyapp.android.ui.group

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.studyapp.android.StudyApp
import com.studyapp.android.data.api.model.repository.Photo
import com.studyapp.android.ui.components.CustomConfirmDialog
import java.io.File
import java.io.FileOutputStream

@Composable
fun PhotoGalleryScreen(
    groupId: Long,
    isLeader: Boolean = false,
    viewModel: PhotoViewModel = viewModel()
) {
    val context = LocalContext.current
    val photoListState by viewModel.photoListState.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()

    var selectedPhoto by remember { mutableStateOf<Photo?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var photoToDelete by remember { mutableStateOf<Photo?>(null) }

    // 이미지 선택 런처
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = uriToFile(context, uri)
            file?.let { viewModel.uploadPhoto(groupId, it, null) }
        }
    }

    LaunchedEffect(groupId) {
        viewModel.getGroupPhotos(groupId)
    }

    // ✅ 로그 추가 - 사진 URL 확인
    LaunchedEffect(photoListState) {
        if (photoListState is PhotoListState.Success) {
            val photos = (photoListState as PhotoListState.Success).photos
            Log.d("PhotoGallery", "=== 사진 목록 (${photos.size}개) ===")
            photos.forEach { photo ->
                Log.d("PhotoGallery", "Photo ID: ${photo.photoId}")
                Log.d("PhotoGallery", "Image URL: ${photo.imageUrl}")
                Log.d("PhotoGallery", "Username: ${photo.username}")
                Log.d("PhotoGallery", "---")
            }
        } else if (photoListState is PhotoListState.Error) {
            Log.e("PhotoGallery", "Error: ${(photoListState as PhotoListState.Error).message}")
        }
    }

    // 업로드 완료 시 다이얼로그 닫기
    LaunchedEffect(uploadState) {
        if (uploadState is UploadState.Success) {
            viewModel.resetUploadState()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = photoListState) {
            is PhotoListState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF6B8CFF))
                }
            }
            is PhotoListState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = { viewModel.getGroupPhotos(groupId) }) {
                            Text("다시 시도", color = Color(0xFF6B8CFF))
                        }
                    }
                }
            }
            is PhotoListState.Success -> {
                if (state.photos.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "업로드된 사진이 없습니다",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "첫 사진을 업로드해보세요!",
                                fontSize = 14.sp,
                                color = Color(0xFF999999)
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(state.photos) { photo ->
                            PhotoItem(
                                photo = photo,
                                onClick = { selectedPhoto = photo }
                            )
                        }
                    }
                }
            }
        }

        // FAB - 사진 추가
        FloatingActionButton(
            onClick = { imagePickerLauncher.launch("image/*") },
            containerColor = Color(0xFF6B8CFF),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, "사진 추가", tint = Color.White)
        }
    }

    // 커스텀 사진 상세 다이얼로그
    selectedPhoto?.let { photo ->
        CustomPhotoDetailDialog(
            photo = photo,
            isLeader = isLeader,
            canDelete = isLeader || photo.userId == StudyApp.currentUserId,
            onDismiss = { selectedPhoto = null },
            onDelete = {
                photoToDelete = photo
                showDeleteDialog = true
            }
        )
    }

    // 삭제 확인 다이얼로그
    if (showDeleteDialog && photoToDelete != null) {
        CustomConfirmDialog(
            title = "사진 삭제",
            message = "정말 이 사진을 삭제하시겠습니까?\n삭제된 사진은 복구할 수 없습니다.",
            confirmText = "삭제",
            dismissText = "취소",
            onConfirm = {
                viewModel.deletePhoto(groupId, photoToDelete!!.photoId)
                selectedPhoto = null
                showDeleteDialog = false
                photoToDelete = null
            },
            onDismiss = {
                showDeleteDialog = false
                photoToDelete = null
            }
        )
    }

    // 업로드 상태 스낵바
    if (uploadState is UploadState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF6B8CFF)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("사진을 업로드 중입니다...")
                }
            }
        }
    }
}

@Composable
fun PhotoItem(
    photo: Photo,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF0F0F0)
    ) {
        // ✅ 이미지 로딩 에러 로그 추가
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photo.imageUrl)
                .crossfade(true)
                .listener(
                    onError = { _, result ->
                        Log.e("PhotoGallery", "이미지 로드 실패: ${photo.imageUrl}")
                        Log.e("PhotoGallery", "Error: ${result.throwable.message}")
                    },
                    onSuccess = { _, _ ->
                        Log.d("PhotoGallery", "이미지 로드 성공: ${photo.imageUrl}")
                    }
                )
                .build(),
            contentDescription = photo.originalFilename ?: "사진",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

// 커스텀 사진 상세 다이얼로그
@Composable
fun CustomPhotoDetailDialog(
    photo: Photo,
    isLeader: Boolean,
    canDelete: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 상단바
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = photo.originalFilename ?: "사진",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "닫기", tint = Color.Black)
                    }
                }

                Divider(color = Color(0xFFE0E0E0))

                // 이미지
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = photo.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Divider(color = Color(0xFFE0E0E0))

                // 정보 영역
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // 설명
                    if (!photo.description.isNullOrBlank()) {
                        Text(
                            text = photo.description ?: "",
                            fontSize = 14.sp,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // 업로더 정보
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "업로드: ${photo.username ?: "알 수 없음"}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = formatFileSize(photo.fileSize ?: 0L),
                                fontSize = 11.sp,
                                color = Color(0xFF999999)
                            )
                        }

                        // 삭제 버튼 (권한 있을 때만)
                        if (canDelete) {
                            Button(
                                onClick = onDelete,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF5252)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isLeader) "삭제 (관리자)" else "삭제",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 파일 크기 포맷
fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${size / 1024} KB"
        else -> String.format("%.1f MB", size / (1024.0 * 1024.0))
    }
}

// URI를 File로 변환하는 유틸리티 함수
private fun uriToFile(context: android.content.Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        file
    } catch (e: Exception) {
        null
    }
}