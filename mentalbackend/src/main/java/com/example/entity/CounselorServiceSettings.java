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
 * 咨询师服务设置表
 */
@Data
@TableName("counselor_service_settings")
public class CounselorServiceSettings implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("counselor_id")
    private Long counselorId;

    @TableField("service_types")
    private String serviceTypes;

    @TableField("available_days")
    private String availableDays;

    @TableField("working_hours")
    private String workingHours;

    @TableField("session_durations")
    private String sessionDurations;

    @TableField("max_daily_sessions")
    private Integer maxDailySessions;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("updated_time")
    private LocalDateTime updatedTime;

}