package com.studyapp.android.data.api.model.repository

data class Message(
    val messageId: Long,
    val groupId: Long,
    val userId: Long,
    val username: String,
    val content: String,
    val createdAt: String
)

data class SendMessageRequest(
    val content: String
)

data class MessageListResponse(
    val success: Boolean = true,
    val messages: List<Message>
)