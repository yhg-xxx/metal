package com.example.dto;

import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
public class LoginResponse {
    private boolean success;       // 是否登录成功
    private String token;          // 登录令牌
    private String message;        // 消息
    private CounselorDTO counselor; // 咨询师信息

}