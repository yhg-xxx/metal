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
 * 学习包表
 */
@Data
@TableName("learning_packages")
public class LearningPackages implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("title")
    private String title;

    @TableField("description")
    private String description;

    @TableField("cover_image_url")
    private String coverImageUrl;

    @TableField("target_tags")
    private String targetTags; // JSON格式存储

    @TableField("video_count")
    private Integer videoCount;

    @TableField("estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @TableField("difficulty_level")
    private String difficultyLevel; // BEGINNER, INTERMEDIATE, ADVANCED

    @TableField("status")
    private String status; // DRAFT, PUBLISHED, ARCHIVED

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("updated_time")
    private LocalDateTime updatedTime;

}