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
 * 快速咨询申请表
 */
@Data
@TableName("quick_consultation_requests")
public class QuickConsultationRequests implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("problem_description")
    private String problemDescription;

    @TableField("problem_duration")
    private String problemDuration;

    @TableField("preferred_method")
    private String preferredMethod;

    @TableField("attached_images")
    private String attachedImages;

    @TableField("matched_counselor_id")
    private Long matchedCounselorId;

    @TableField("status")
    private String status;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("matched_time")
    private LocalDateTime matchedTime;

}