package com.example.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 咨询师信息DTO，整合了咨询师的全部相关信息
 */
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

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public Long getCounselorId() {
        return counselorId;
    }

    public void setCounselorId(Long counselorId) {
        this.counselorId = counselorId;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getQualificationCertificateUrl() {
        return qualificationCertificateUrl;
    }

    public void setQualificationCertificateUrl(String qualificationCertificateUrl) {
        this.qualificationCertificateUrl = qualificationCertificateUrl;
    }

    public String getPracticeCertificateUrl() {
        return practiceCertificateUrl;
    }

    public void setPracticeCertificateUrl(String practiceCertificateUrl) {
        this.practiceCertificateUrl = practiceCertificateUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getTherapeuticApproach() {
        return therapeuticApproach;
    }

    public void setTherapeuticApproach(String therapeuticApproach) {
        this.therapeuticApproach = therapeuticApproach;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public Integer getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(Integer totalSessions) {
        this.totalSessions = totalSessions;
    }

    public String getCounselorStatus() {
        return counselorStatus;
    }

    public void setCounselorStatus(String counselorStatus) {
        this.counselorStatus = counselorStatus;
    }

    public LocalDateTime getApprovedTime() {
        return approvedTime;
    }

    public void setApprovedTime(LocalDateTime approvedTime) {
        this.approvedTime = approvedTime;
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

    public Long getServiceSettingsId() {
        return serviceSettingsId;
    }

    public void setServiceSettingsId(Long serviceSettingsId) {
        this.serviceSettingsId = serviceSettingsId;
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

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<String> getSpecializationTags() {
        return specializationTags;
    }

    public void setSpecializationTags(List<String> specializationTags) {
        this.specializationTags = specializationTags;
    }

    public List<String> getTherapeuticApproachTags() {
        return therapeuticApproachTags;
    }

    public void setTherapeuticApproachTags(List<String> therapeuticApproachTags) {
        this.therapeuticApproachTags = therapeuticApproachTags;
    }

    public List<String> getServiceTypeTags() {
        return serviceTypeTags;
    }

    public void setServiceTypeTags(List<String> serviceTypeTags) {
        this.serviceTypeTags = serviceTypeTags;
    }

    public String getGenderFilter() {
        return genderFilter;
    }

    public void setGenderFilter(String genderFilter) {
        this.genderFilter = genderFilter;
    }
}