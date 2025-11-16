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
 * 咨询师信息表
 */
@Data
@TableName("counselors")
public class Counselors implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("real_name")
    private String realName;

    @TableField("id_number")
    private String idNumber;

    @TableField("qualification_certificate_url")
    private String qualificationCertificateUrl;

    @TableField("practice_certificate_url")
    private String practiceCertificateUrl;

    @TableField("photo_url")
    private String photoUrl;

    @TableField("years_of_experience")
    private Integer yearsOfExperience;

    @TableField("specialization")
    private String specialization;

    @TableField("therapeutic_approach")
    private String therapeuticApproach;

    @TableField("introduction")
    private String introduction;

    @TableField("consultation_fee")
    private BigDecimal consultationFee;

    @TableField("rating")
    private BigDecimal rating;

    @TableField("total_sessions")
    private Integer totalSessions;

    @TableField("status")
    private String status;

    @TableField("approved_time")
    private LocalDateTime approvedTime;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("updated_time")
    private LocalDateTime updatedTime;
}