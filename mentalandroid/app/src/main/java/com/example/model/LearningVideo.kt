package com.example.model

/**
 * 学习视频数据模型
 */
data class LearningVideo(
    val id: Long,
    val learningPackageId: Long,
    val title: String,
    val description: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val durationSeconds: Int,
    val sortOrder: Int,
    val status: String,
    val createdTime: String
)