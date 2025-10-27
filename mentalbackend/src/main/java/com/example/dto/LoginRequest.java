package com.example.dto;

/**
 * 登录请求DTO
 */
public class LoginRequest {
    private String username;    // 用户名
    private String password;    // 密码

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}