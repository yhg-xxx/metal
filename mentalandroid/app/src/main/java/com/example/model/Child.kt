package com.example.model

/**
 * 孩子信息数据模型
 * 根据后端API接口文档定义
 */
import com.google.gson.annotations.SerializedName

/**
 * 孩子信息数据模型
 * 根据后端API接口文档定义
 */
data class Child(
    val id: Long = 0,
    val userId: Long = 0,
    val name: String = "",
    val gender: String = "",

    @SerializedName("birthDate")
    val birthYearMonth: String? = null,

    val ethnicity: String? = null,

    @SerializedName("nativePlace")
    val householdRegister: String? = null,

    val birthOrder: String? = null,

    @SerializedName("birthLocation")
    val birthPlace: String? = null,

    val languageEnvironment: String? = null,
    val currentSchool: String? = null,
    val homeAddress: String? = null,

    @SerializedName("hobbies")
    val habits: String? = null,

    @SerializedName("interests")
    val interestActivities: String? = null,

    val healthStatus: String? = null,
    val healthDescription: String? = null,

    @SerializedName("pastDiseases")
    val pastIllness: String? = null,

    val pastIllnessDescription: String? = null,
    val fatherPhone: String? = null,
    val motherPhone: String? = null,
    val guardianPhone: String? = null,
    val isCurrentOperation: Boolean = false
)

/**
 * 用于创建孩子信息的请求模型
 */
data class CreateChildRequest(
    val userId: Long,
    val name: String,
    val gender: String,

    @SerializedName("birthDate")
    val birthYearMonth: String? = null,

    val ethnicity: String? = null,

    @SerializedName("nativePlace")
    val householdRegister: String? = null,

    val birthOrder: String? = null,

    @SerializedName("birthLocation")
    val birthPlace: String? = null,

    val languageEnvironment: String? = null,
    val currentSchool: String? = null,
    val homeAddress: String? = null,

    @SerializedName("hobbies")
    val habits: String? = null,

    @SerializedName("interests")
    val interestActivities: String? = null,

    val healthStatus: String? = null,
    val healthDescription: String? = null,

    @SerializedName("pastDiseases")
    val pastIllness: String? = null,

    val pastIllnessDescription: String? = null,
    val fatherPhone: String? = null,
    val motherPhone: String? = null,
    val guardianPhone: String? = null
)

/**
 * 用于更新孩子信息的请求模型
 */
data class UpdateChildRequest(
    val id: Long,
    val name: String? = null,
    val gender: String? = null,

    @SerializedName("birthDate")
    val birthYearMonth: String? = null,

    val ethnicity: String? = null,

    @SerializedName("nativePlace")
    val householdRegister: String? = null,

    val birthOrder: String? = null,

    @SerializedName("birthLocation")
    val birthPlace: String? = null,

    val languageEnvironment: String? = null,
    val currentSchool: String? = null,
    val homeAddress: String? = null,

    @SerializedName("hobbies")
    val habits: String? = null,

    @SerializedName("interests")
    val interestActivities: String? = null,

    val healthStatus: String? = null,
    val healthDescription: String? = null,

    @SerializedName("pastDiseases")
    val pastIllness: String? = null,

    val pastIllnessDescription: String? = null,
    val fatherPhone: String? = null,
    val motherPhone: String? = null,
    val guardianPhone: String? = null
)