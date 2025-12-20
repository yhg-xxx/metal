package com.example.model

/**
 * 心理评估报告数据模型
 */
data class PsychologicalAssessments(
    val id: Long,
    val userId: Long,
    val testRecordId: Long,
    val consultationRecordId: Long?,
    val overallScore: Double,
    val knowledgeMasteryRate: Double,
    val psychologicalStateIndicators: String,
    val improvementSuggestions: String?,
    val recommendedActions: String?,
    val assessmentDate: String,
    val createdTime: String
)
