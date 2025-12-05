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
    
    /**
     * 检查手机号是否已注册
     * @param phone 手机号
     * @return 是否已注册
     */
    boolean checkPhoneExists(String phone);
    
    /**
     * 用户注册
     * @param user 用户信息
     * @return 注册后的用户信息
     */
    Users registerUser(Users user);
    
    /**
     * 用户登录
     * @param phone 手机号
     * @param password 密码
     * @return 用户信息
     */
    Users loginUser(String phone, String password);
}