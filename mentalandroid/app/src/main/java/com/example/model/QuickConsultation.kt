package com.example.model

import com.google.gson.annotations.SerializedName

/**
 * 快速咨询数据模型
 */
data class QuickConsultation(
    @SerializedName("id")
    val id: Long, // 快速咨询ID
    
    @SerializedName("userId")
    val userId: Long, // 用户ID
    
    @SerializedName("problemDescription")
    val problemDescription: String, // 问题描述
    
    @SerializedName("problemDuration")
    val problemDuration: String, // 问题持续时间
    
    @SerializedName("preferredMethod")
    val preferredMethod: String, // 偏好咨询方式：TEXT（文字）/VOICE（语音）/VIDEO（视频）
    
    @SerializedName("attachedImages")
    val attachedImages: String? = null, // 附加图片JSON字符串
    
    @SerializedName("matchedCounselorId")
    val matchedCounselorId: Long? = null, // 匹配的咨询师ID
    
    @SerializedName("status")
    val status: String, // 状态：PENDING（待处理）
    
    @SerializedName("createdTime")
    val createdTime: String, // 创建时间
    
    @SerializedName("matchedTime")
    val matchedTime: String? = null // 匹配时间
)

/**
 * 附加图片数据类
 */
data class AttachedImage(
    val url: String, // 图片URL
    val recognizedText: String // 识别的文字内容
)