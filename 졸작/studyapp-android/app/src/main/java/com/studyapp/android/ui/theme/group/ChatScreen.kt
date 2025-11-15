package com.studyapp.android.ui.group

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studyapp.android.StudyApp
import com.studyapp.android.data.api.model.repository.Message
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    groupId: Long,
    viewModel: ChatViewModel = viewModel()
) {
    var messageText by remember { mutableStateOf("") }
    val messagesState by viewModel.messagesState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 메시지 로드 및 Polling 시작
    LaunchedEffect(groupId) {
        viewModel.startLoadingMessages(groupId)
        viewModel.startPolling(groupId)
    }

    // 컴포넌트 종료 시 Polling 중지
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopPolling()
        }
    }

    // 새 메시지 오면 스크롤
    LaunchedEffect(messagesState) {
        if (messagesState is MessagesState.Success) {
            val messages = (messagesState as MessagesState.Success).messages
            if (messages.isNotEmpty()) {
                coroutineScope.launch {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // 메시지 목록
        when (val state = messagesState) {
            is MessagesState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF6B8CFF))
                }
            }
            is MessagesState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = { viewModel.startLoadingMessages(groupId) }) {
                            Text("다시 시도", color = Color(0xFF6B8CFF))
                        }
                    }
                }
            }
            is MessagesState.Success -> {
                if (state.messages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "첫 메시지를 보내보세요!",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.messages) { message ->
                            MessageItem(
                                message = message,
                                isMyMessage = message.userId == StudyApp.currentUserId
                            )
                        }
                    }
                }
            }
        }

        // 입력창
        Surface(
            modifier = Modifier.fillMaxWidth(),  // ✅ 이것만!
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),  // ✅ vertical을 8dp로!
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("메시지를 입력하세요") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color(0xFF6B8CFF),
                        unfocusedIndicatorColor = Color(0xFFE0E0E0),
                        cursorColor = Color(0xFF6B8CFF)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendMessage(groupId, messageText)
                            messageText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6B8CFF)
                    ),
                    enabled = messageText.isNotBlank()
                ) {
                    Text("전송")
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: Message, isMyMessage: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
    ) {
        if (!isMyMessage) {
            // 상대방 프로필
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message.username.first().toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isMyMessage) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            if (!isMyMessage) {
                Text(
                    text = message.username,
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isMyMessage) Color(0xFF6B8CFF) else Color.White,
                shadowElevation = if (isMyMessage) 0.dp else 1.dp
            ) {
                Text(
                    text = message.content,
                    fontSize = 15.sp,
                    color = if (isMyMessage) Color.White else Color.Black,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Text(
                text = formatTime(message.createdAt),
                fontSize = 11.sp,
                color = Color(0xFF999999),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

fun formatTime(dateString: String): String {
    return try {
        dateString.substring(11, 16) // "HH:mm"
    } catch (e: Exception) {
        dateString
    }
}