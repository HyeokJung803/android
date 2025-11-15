package com.studyapp.android.ui.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyapp.android.StudyApp
import com.studyapp.android.data.api.model.repository.ChatRepository
import com.studyapp.android.data.api.model.repository.Message
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val repository = ChatRepository()

    private val currentUserId: Long
        get() = StudyApp.currentUserId

    private val _messagesState = MutableStateFlow<MessagesState>(MessagesState.Loading)
    val messagesState: StateFlow<MessagesState> = _messagesState

    private var pollingJob: Job? = null
    private var lastMessageTime: String? = null

    // 메시지 조회 시작
    fun startLoadingMessages(groupId: Long) {
        viewModelScope.launch {
            _messagesState.value = MessagesState.Loading
            val result = repository.getMessages(groupId)

            _messagesState.value = if (result.isSuccess) {
                val messages = result.getOrNull()!!
                if (messages.isNotEmpty()) {
                    lastMessageTime = messages.first().createdAt
                }
                MessagesState.Success(messages.reversed()) // 오래된 것부터
            } else {
                MessagesState.Error(result.exceptionOrNull()?.message ?: "메시지 조회 실패")
            }
        }
    }

    // Polling 시작 (3초마다)
    fun startPolling(groupId: Long) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(3000) // 3초마다

                lastMessageTime?.let { after ->
                    val result = repository.getMessagesAfter(groupId, after)

                    if (result.isSuccess) {
                        val newMessages = result.getOrNull()!!
                        if (newMessages.isNotEmpty()) {
                            // 새 메시지가 있으면 추가
                            val currentState = _messagesState.value
                            if (currentState is MessagesState.Success) {
                                val updatedMessages = currentState.messages + newMessages
                                _messagesState.value = MessagesState.Success(updatedMessages)
                                lastMessageTime = newMessages.last().createdAt
                            }
                        }
                    }
                }
            }
        }
    }

    // Polling 중지
    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    // 메시지 전송
    fun sendMessage(groupId: Long, content: String) {
        viewModelScope.launch {
            val result = repository.sendMessage(groupId, currentUserId, content)

            if (result.isSuccess) {
                val newMessage = result.getOrNull()!!
                val currentState = _messagesState.value
                if (currentState is MessagesState.Success) {
                    val updatedMessages = currentState.messages + newMessage
                    _messagesState.value = MessagesState.Success(updatedMessages)
                    lastMessageTime = newMessage.createdAt
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}

sealed class MessagesState {
    object Loading : MessagesState()
    data class Success(val messages: List<Message>) : MessagesState()
    data class Error(val message: String) : MessagesState()
}