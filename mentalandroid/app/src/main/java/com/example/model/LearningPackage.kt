package com.example.model

/**
 * 学习包数据模型
 */
data class LearningPackage(
    val id: Int,
    val title: String,
    val description: String,
    val coverImageUrl: String?,
    val targetTags: String?,
    val videoCount: Int,
    val estimatedDurationMinutes: Int,
    val difficultyLevel: String,
    val status: String,
    val createdTime: String?,
    val updatedTime: String?
)