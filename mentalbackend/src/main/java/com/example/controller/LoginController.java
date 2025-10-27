package com.example.controller;

import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;
import com.example.service.UsersService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 登录控制器
 */
@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Resource
    private UsersService usersService;

    /**
     * 咨询师登录接口
     * @param loginRequest 登录请求信息
     * @return 登录响应结果
     */
    @PostMapping("/counselor/login")
    public ResponseEntity<LoginResponse> counselorLogin(@RequestBody LoginRequest loginRequest) {
        // 调用服务层方法进行登录验证
        LoginResponse response = usersService.counselorLogin(loginRequest);
        
        // 根据登录结果返回不同的HTTP状态码
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}