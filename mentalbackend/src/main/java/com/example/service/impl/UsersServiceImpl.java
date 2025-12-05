package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;
import com.example.service.CounselorsService;
import com.example.dto.CounselorDTO;
import com.example.entity.Users;
import com.example.mapper.UsersMapper;
import com.example.service.UsersService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户服务实现类
 */
@Service
@Slf4j
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements UsersService {

    @Resource
    private CounselorsService counselorsService;

    @Override
    public LoginResponse counselorLogin(LoginRequest loginRequest) {
        LoginResponse response = new LoginResponse();

        // 验证输入参数
        if (loginRequest == null || loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            response.setSuccess(false);
            response.setMessage("用户名和密码不能为空");
            return response;
        }

        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        // 根据用户名查询用户
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        Users user = this.getOne(queryWrapper);

        // 验证用户是否存在
        if (user == null) {
            response.setSuccess(false);
            response.setMessage("用户名不存在");
            return response;
        }

        // 验证是否是咨询师（根据用户名前缀判断）
        if (!username.startsWith("counselor_")) {
            response.setSuccess(false);
            response.setMessage("该账号不是咨询师账号");
            return response;
        }

        // 验证密码（在实际项目中应该使用更安全的密码加密方式）
        // 这里假设数据库中存储的是明文密码，实际应该使用加密后的密码进行比对
        if (!password.equals(user.getPassword())) {
            // 尝试使用MD5加密后比对（如果数据库中是MD5加密的密码）
            String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
            if (!encryptedPassword.equals(user.getPassword())) {
                response.setSuccess(false);
                response.setMessage("密码错误");
                return response;
            }
        }

        // 验证用户状态
        if (!"ACTIVE".equals(user.getStatus())) {
            response.setSuccess(false);
            response.setMessage("账号已被禁用");
            return response;
        }

        try {
            // 根据用户ID查询咨询师信息
            // 这里需要找到对应的咨询师ID
            CounselorDTO counselorDTO = new CounselorDTO();
            counselorDTO.setUserId(user.getId());
            counselorDTO = counselorsService.getCounselorDetailByUserId(user.getId());

            if (counselorDTO == null) {
                response.setSuccess(false);
                response.setMessage("咨询师信息不存在");
                return response;
            }



            // 生成登录令牌（简单示例，实际项目中应使用更安全的令牌生成方式）
            String token = "token_" + UUID.randomUUID().toString().replace("-", "");

            // 设置响应信息
            response.setSuccess(true);
            response.setToken(token);
            response.setMessage("登录成功");
            response.setCounselor(counselorDTO);

            log.info("咨询师登录成功: username={}, userId={}, counselorId={}", 
                    username, user.getId(), counselorDTO.getCounselorId());

        } catch (Exception e) {
            log.error("咨询师登录失败: username={}, error={}", username, e.getMessage());
            response.setSuccess(false);
            response.setMessage("登录失败，请稍后重试");
        }

        return response;
    }

    @Override
    public boolean checkPhoneExists(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        return this.exists(queryWrapper);
    }

    @Override
    public Users registerUser(Users user) {
        if (user == null || user.getPhone() == null || user.getPhone().isEmpty()) {
            throw new IllegalArgumentException("用户信息或手机号不能为空");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }

        // 检查手机号是否已注册
        if (checkPhoneExists(user.getPhone())) {
            throw new IllegalArgumentException("手机号已注册");
        }

        // 如果用户名未提供，生成默认用户名
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            user.setUsername("user_" + user.getPhone());
        }

        // 密码已经是MD5加密的，直接存储
        // user.setPassword(user.getPassword()); // 不需要再次加密

        // 设置默认值
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedTime(now);
        user.setUpdatedTime(now);
        user.setStatus("ACTIVE");

        // 保存用户信息
        this.save(user);
        log.info("用户注册成功: phone={}, userId={}", user.getPhone(), user.getId());
        return user;
    }

    @Override
    public Users loginUser(String phone, String password) {
        if (phone == null || phone.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("手机号和密码不能为空");
        }

        // 根据手机号查询用户
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        Users user = this.getOne(queryWrapper);

        // 验证用户是否存在
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 验证密码（传入的密码已经是MD5加密的）
        if (!password.equals(user.getPassword())) {
            throw new IllegalArgumentException("密码错误");
        }

        // 验证用户状态
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new IllegalArgumentException("账号已被禁用");
        }

        log.info("用户登录成功: phone={}, userId={}", phone, user.getId());
        return user;
    }
}