package com.example.model

/**
 * 测试题库数据模型
 */
data class TestQuestions(
    val id: Long,
    val learningPackageId: Long,
    val questionType: String,
    val questionText: String,
    val options: String,
    val correctAnswers: String,
    val explanation: String?,
    val points: Int,
    val psychologicalDimension: String?,
    val sortOrder: Int,
    val status: String,
    val createdTime: String?
)
