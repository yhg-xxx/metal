package com.example.model

/**
 * 用户数据模型
 */
data class User(
    val id: Int = 0,
    val username: String,
    val phone: String,
    val password: String,
    val email: String? = null,
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val gender: String = "UNKNOWN", // MALE, FEMALE, UNKNOWN
    val age: Int? = null,
    val status: String? = "ACTIVE", // ACTIVE, INACTIVE, BANNED
    val createdTime: String? = null,
    val updatedTime: String? = null,
    val isLogin: Boolean = false
)