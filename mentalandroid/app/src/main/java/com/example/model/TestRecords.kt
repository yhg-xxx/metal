package com.example.model

/**
 * 测试记录数据模型
 */
data class TestRecords(
    val id: Long,
    val userId: Long,
    val learningPackageId: Long,
    val totalQuestions: Int,
    val answeredQuestions: Int,
    val correctAnswers: Int,
    val score: Double,
    val timeSpentSeconds: Int,
    val timeLimitSeconds: Int,
    val status: String,
    val startedTime: String,
    val submittedTime: String?
)
