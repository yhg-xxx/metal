package com.example.util

/**
 * IP地址管理工具类
 * 用于统一管理和替换图片URL中的IP地址
 */
object IpAddressManager {
    
    // 当前后端服务器IP地址
    // 注意：当IP地址变更时，只需修改此常量即可
    const val CURRENT_IP_ADDRESS = "192.168.209.109"

    // 后端服务器端口
    const val SERVER_PORT = "8085"
    
    // 完整的服务器基础URL
    const val BASE_URL = "http://$CURRENT_IP_ADDRESS:$SERVER_PORT"
    
    /**
     * 处理图片URL，将localhost或旧的IP地址替换为当前的IP地址
     * @param url 原始图片URL
     * @return 处理后的图片URL
     */
    fun processImageUrl(url: String?): String? {
        if (url == null) return null
        
        // 替换localhost为当前IP地址
        var processedUrl = url.replace("http://localhost:$SERVER_PORT", BASE_URL)
        

        return processedUrl
    }
    
    /**
     * 处理视频URL，将localhost或旧的IP地址替换为当前的IP地址
     * @param url 原始视频URL
     * @return 处理后的视频URL
     */
    fun processVideoUrl(url: String?): String? {
        // 视频URL处理与图片URL处理逻辑相同，可以复用图片URL处理方法
        return processImageUrl(url)
    }
}