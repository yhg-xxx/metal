package com.example.dto;

/**
 * 登录响应DTO
 */
public class LoginResponse {
    private boolean success;       // 是否登录成功
    private String token;          // 登录令牌
    private String message;        // 消息
    private CounselorDTO counselor; // 咨询师信息

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public CounselorDTO getCounselor() {
        return counselor;
    }

    public void setCounselor(CounselorDTO counselor) {
        this.counselor = counselor;
    }
}