package com.example.model

/**
 * 基础响应模型
 */
data class BaseResponse<T>(
    val code: Int, // 响应码，200表示成功
    val msg: String, // 响应消息
    val data: T? = null // 响应数据
)