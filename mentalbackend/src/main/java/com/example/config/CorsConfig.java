package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置类
 * 用于处理跨域资源共享(CORS)，允许前端应用从不同的域名访问后端API
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允许所有请求路径的跨域请求
        registry.addMapping("/**")
                // 使用allowedOriginPatterns代替allowedOrigins，允许所有源
                .allowedOriginPatterns("*")
                // 允许的HTTP方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许的请求头
                .allowedHeaders("*")
                // 允许暴露的响应头
                .exposedHeaders("Content-Type", "Authorization")
                // 是否允许发送Cookie
                .allowCredentials(true)
                // 预检请求的有效期（秒）
                .maxAge(3600);
    }
}