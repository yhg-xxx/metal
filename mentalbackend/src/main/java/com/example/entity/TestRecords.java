package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 测试记录表
 */
@Data
@TableName("test_records")
public class TestRecords implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("learning_package_id")
    private Long learningPackageId;

    @TableField("total_questions")
    private Integer totalQuestions;

    @TableField("answered_questions")
    private Integer answeredQuestions;

    @TableField("correct_answers")
    private Integer correctAnswers;

    @TableField("score")
    private BigDecimal score;

    @TableField("time_spent_seconds")
    private Integer timeSpentSeconds;

    @TableField("time_limit_seconds")
    private Integer timeLimitSeconds;

    @TableField("status")
    private String status;

    @TableField("started_time")
    private LocalDateTime startedTime;

    @TableField("submitted_time")
    private LocalDateTime submittedTime;
}
