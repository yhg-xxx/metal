package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;
import com.example.entity.Users;

/**
 * 用户服务接口
 */
public interface UsersService extends IService<Users> {
    /**
     * 咨询师登录
     * @param loginRequest 登录请求信息
     * @return 登录响应信息
     */
    LoginResponse counselorLogin(LoginRequest loginRequest);
}