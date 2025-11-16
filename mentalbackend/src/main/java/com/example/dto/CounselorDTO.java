package com.example.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 咨询师信息DTO，整合了咨询师的全部相关信息
 */
@Data
public class CounselorDTO {
    // 用户信息
    private Long userId;
    private String username;
    private String phone;
    private String email;
    private String nickname;
    private String avatarUrl;
    private String gender;
    private Integer age;
    private String userStatus;

    // 咨询师信息
    private Long counselorId;
    private String realName;
    private String idNumber;
    private String qualificationCertificateUrl;
    private String practiceCertificateUrl;
    private String photoUrl;
    private Integer yearsOfExperience;
    private String specialization;
    private String therapeuticApproach;
    private String introduction;
    private BigDecimal consultationFee;
    private BigDecimal rating;
    private Integer totalSessions;
    private String counselorStatus;
    private LocalDateTime approvedTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    // 咨询师服务设置信息
    private Long serviceSettingsId;
    private String serviceTypes;
    private String availableDays;
    private String workingHours;
    private String sessionDurations;
    private Integer maxDailySessions;

    // 搜索和筛选条件
    private String keyword;
    private List<String> specializationTags;
    private List<String> therapeuticApproachTags;
    private List<String> serviceTypeTags;
    private String genderFilter;


}