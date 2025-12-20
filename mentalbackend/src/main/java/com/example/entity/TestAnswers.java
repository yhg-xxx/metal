package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 测试答题详情表
 */
@Data
@TableName("test_answers")
public class TestAnswers implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("test_record_id")
    private Long testRecordId;

    @TableField("question_id")
    private Long questionId;

    @TableField("user_answers")
    private String userAnswers;

    @TableField("is_correct")
    private Integer isCorrect;

    @TableField("time_spent_seconds")
    private Integer timeSpentSeconds;

    @TableField("answered_time")
    private LocalDateTime answeredTime;
}
