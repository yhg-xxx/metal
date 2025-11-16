package com.example.network

import com.google.gson.annotations.SerializedName

/**
 * 通用API响应数据模型
 * @param T 响应数据的类型
 */
data class ApiResponse<T>(
    /**
     * 状态码，200表示成功，400表示参数错误，404表示资源不存在，500表示服务器错误
     */
    @SerializedName("code")
    val code: Int,
    
    /**
     * 响应消息
     */
    @SerializedName("msg")
    val message: String,
    
    /**
     * 响应数据
     */
    @SerializedName("data")
    val data: T? = null
) {
    /**
     * 检查是否请求成功
     */
    fun isSuccess(): Boolean {
        return code == 200
    }
}