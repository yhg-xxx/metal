package com.example.model

/**
 * 搜索咨询师请求参数
 */
data class SearchCounselorsRequest(
    val keyword: String? = null, // 搜索关键词（姓名、擅长领域等）
    val specializationTags: List<String>? = null, // 擅长领域标签列表
    val therapeuticApproachTags: List<String>? = null, // 治疗流派标签列表
    val serviceTypeTags: List<String>? = null, // 服务类型标签列表
    val genderFilter: String? = null // 性别筛选（MALE/FEMALE/UNKNOWN）
)