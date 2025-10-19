package com.example.model

/**
 * 聊天消息实体类
 * 用于表示用户和咨询师之间的聊天消息
 */
data class Message(
    val id: Long,
    val appointmentId: Long?,
    val senderType: String,
    val messageType: String,
    val content: String,
    val mediaUrl: String?,
    val durationSeconds: Int?,
    val sentTime: String,
    val readStatus: Boolean,
    val userId: Long,
    val counselorId: Long,
    val conversationType: String
)