package com.example.model

/**
 * API响应数据模型
 */
sealed class ApiResponse<out T> {
    data class Success<T>(val code: Int, val msg: String, val data: T) : ApiResponse<T>()
    data class Error(val code: Int, val msg: String) : ApiResponse<Nothing>()
}