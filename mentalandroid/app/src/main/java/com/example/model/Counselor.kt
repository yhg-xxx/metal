package com.example.model

import com.google.gson.annotations.SerializedName

/**
 * 咨询师数据模型
 */
data class Counselor(
    @SerializedName("counselorId")
    val counselorId: Int, // 咨询师ID
    
    @SerializedName("userId")
    val userId: Int, // 用户ID
    
    @SerializedName("realName")
    val realName: String, // 真实姓名
    
    @SerializedName("username")
    val username: String, // 用户名
    
    @SerializedName("phone")
    val phone: String, // 手机号
    
    @SerializedName("email")
    val email: String, // 邮箱
    
    @SerializedName("gender")
    val gender: String, // 性别
    
    @SerializedName("age")
    val age: Int, // 年龄
    
    @SerializedName("qualificationCertificateUrl")
    val qualificationCertificateUrl: String? = null, // 资质证书URL
    
    @SerializedName("practiceCertificateUrl")
    val practiceCertificateUrl: String? = null, // 执业证书URL
    
    @SerializedName("avatarUrl") // 服务器返回的是avatarUrl，我们映射到photoUrl
    val photoUrl: String? = null, // 头像URL
    
    @SerializedName("yearsOfExperience")
    val yearsOfExperience: Int, // 从业年限
    
    @SerializedName("specialization")
    val specialization: String, // 擅长领域
    
    @SerializedName("therapeuticApproach")
    val therapeuticApproach: String? = null, // 治疗流派
    
    @SerializedName("introduction")
    val introduction: String? = null, // 个人介绍
    
    @SerializedName("consultationFee")
    val consultationFee: Double, // 咨询费用
    
    @SerializedName("rating")
    val rating: Double, // 平均评分
    
    @SerializedName("totalSessions")
    val totalSessions: Int, // 总咨询次数
    
    @SerializedName("counselorStatus")
    val counselorStatus: String? = null, // 咨询师状态
    
    @SerializedName("serviceTypes")
    val serviceTypes: String? = null, // 服务类型
    
    @SerializedName("availableDays")
    val availableDays: String? = null, // 可用日期
    
    @SerializedName("workingHours")
    val workingHours: String? = null, // 工作时间段
    
    @SerializedName("sessionDurations")
    val sessionDurations: String? = null, // 支持的咨询时长
    
    @SerializedName("maxDailySessions")
    val maxDailySessions: Int? = null, // 每日最大咨询次数
    
    @SerializedName("createdTime")
    val createdTime: String? = null, // 创建时间
    
    @SerializedName("updatedTime")
    val updatedTime: String? = null // 更新时间
)