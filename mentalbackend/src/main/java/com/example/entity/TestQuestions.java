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
 * 测试题库表
 */
@Data
@TableName("test_questions")
public class TestQuestions implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("learning_package_id")
    private Long learningPackageId;

    @TableField("question_type")
    private String questionType;

    @TableField("question_text")
    private String questionText;

    @TableField("options")
    private String options;

    @TableField("correct_answers")
    private String correctAnswers;

    @TableField("explanation")
    private String explanation;

    @TableField("points")
    private Integer points;

    @TableField("psychological_dimension")
    private String psychologicalDimension;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("status")
    private String status;

    @TableField("created_time")
    private LocalDateTime createdTime;
}
