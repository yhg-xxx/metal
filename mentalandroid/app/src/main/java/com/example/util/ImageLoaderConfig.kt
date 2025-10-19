package com.example.util

import android.content.Context
import coil.ImageLoader
import coil.util.DebugLogger
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Coil图片加载器配置
 */
object ImageLoaderConfig {
    
    /**
     * 创建配置好的ImageLoader实例
     */
    fun createImageLoader(context: Context): ImageLoader {
        // 创建专用的OkHttpClient
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
        
        // 创建并配置ImageLoader
        return ImageLoader.Builder(context)
            .okHttpClient { okHttpClient }
            .crossfade(true)
            .respectCacheHeaders(false) // 忽略缓存控制头，始终尝试加载最新图片
            .logger(DebugLogger()) // 始终启用日志以便调试图片加载问题
            .build()
    }
}