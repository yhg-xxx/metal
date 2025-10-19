package com.example.network

import com.example.util.IpAddressManager
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    // 后端服务器的基础URL，使用统一的IP地址管理工具
    private const val BASE_URL = IpAddressManager.BASE_URL
    
    // Gson配置
    private val gson: Gson by lazy {
        GsonBuilder()
            .setLenient()
            .serializeNulls() // 序列化null值
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY) // 使用字段原名
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss") // 设置日期格式
            .create()
    }
    
    // OkHttp客户端配置
    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // 设置日志级别为BODY，显示完整的请求和响应信息
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    // Retrofit实例配置
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    // ApiService实例
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}