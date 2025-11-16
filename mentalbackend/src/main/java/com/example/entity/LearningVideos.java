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
 * 学习视频表
 */
@Data
@TableName("learning_videos")
public class LearningVideos implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("learning_package_id")
    private Long learningPackageId;

    @TableField("title")
    private String title;

    @TableField("description")
    private String description;

    @TableField("video_url")
    private String videoUrl;

    @TableField("thumbnail_url")
    private String thumbnailUrl;

    @TableField("duration_seconds")
    private Integer durationSeconds;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("status")
    private String status; // DRAFT, PUBLISHED, ARCHIVED

    @TableField("created_time")
    private LocalDateTime createdTime;

}