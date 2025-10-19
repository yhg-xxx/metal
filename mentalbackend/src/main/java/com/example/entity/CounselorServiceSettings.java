package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 咨询师服务设置表
 */
@TableName("counselor_service_settings")
public class CounselorServiceSettings implements Serializable {
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

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCounselorId() {
        return counselorId;
    }

    public void setCounselorId(Long counselorId) {
        this.counselorId = counselorId;
    }

    public String getServiceTypes() {
        return serviceTypes;
    }

    public void setServiceTypes(String serviceTypes) {
        this.serviceTypes = serviceTypes;
    }

    public String getAvailableDays() {
        return availableDays;
    }

    public void setAvailableDays(String availableDays) {
        this.availableDays = availableDays;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    public String getSessionDurations() {
        return sessionDurations;
    }

    public void setSessionDurations(String sessionDurations) {
        this.sessionDurations = sessionDurations;
    }

    public Integer getMaxDailySessions() {
        return maxDailySessions;
    }

    public void setMaxDailySessions(Integer maxDailySessions) {
        this.maxDailySessions = maxDailySessions;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }
}