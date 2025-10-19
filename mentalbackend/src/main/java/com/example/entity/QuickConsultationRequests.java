package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 快速咨询申请表
 */
@TableName("quick_consultation_requests")
public class QuickConsultationRequests implements Serializable {
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

    // getter and setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getProblemDescription() {
        return problemDescription;
    }

    public void setProblemDescription(String problemDescription) {
        this.problemDescription = problemDescription;
    }

    public String getProblemDuration() {
        return problemDuration;
    }

    public void setProblemDuration(String problemDuration) {
        this.problemDuration = problemDuration;
    }

    public String getPreferredMethod() {
        return preferredMethod;
    }

    public void setPreferredMethod(String preferredMethod) {
        this.preferredMethod = preferredMethod;
    }

    public String getAttachedImages() {
        return attachedImages;
    }

    public void setAttachedImages(String attachedImages) {
        this.attachedImages = attachedImages;
    }

    public Long getMatchedCounselorId() {
        return matchedCounselorId;
    }

    public void setMatchedCounselorId(Long matchedCounselorId) {
        this.matchedCounselorId = matchedCounselorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getMatchedTime() {
        return matchedTime;
    }

    public void setMatchedTime(LocalDateTime matchedTime) {
        this.matchedTime = matchedTime;
    }
}