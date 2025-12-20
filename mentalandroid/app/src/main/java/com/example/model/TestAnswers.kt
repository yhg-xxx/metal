package com.example.model

/**
 * 测试答题详情数据模型
 */
data class TestAnswers(
    val id: Long,
    val testRecordId: Long,
    val questionId: Long,
    val userAnswers: String,
    val isCorrect: Int,
    val timeSpentSeconds: Int,
    val answeredTime: String
)
