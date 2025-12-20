package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 心理评估报告表
 */
@Data
@TableName("psychological_assessments")
public class PsychologicalAssessments implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("test_record_id")
    private Long testRecordId;

    @TableField("consultation_record_id")
    private Long consultationRecordId;

    @TableField("overall_score")
    private BigDecimal overallScore;

    @TableField("knowledge_mastery_rate")
    private BigDecimal knowledgeMasteryRate;

    @TableField("psychological_state_indicators")
    private String psychologicalStateIndicators;

    @TableField("improvement_suggestions")
    private String improvementSuggestions;

    @TableField("recommended_actions")
    private String recommendedActions;

    @TableField("assessment_date")
    private LocalDate assessmentDate;

    @TableField("created_time")
    private LocalDateTime createdTime;
}
