package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 咨询师信息表
 */
@TableName("counselors")
public class Counselors implements Serializable {
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

    // Getters and Setters
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
}