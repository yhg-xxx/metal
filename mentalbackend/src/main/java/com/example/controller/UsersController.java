package com.example.controller;

import com.example.entity.Users;
import com.example.service.UsersService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/users")
public class UsersController {

    @Resource
    private UsersService usersService;

    @Resource
    private FileUploadController fileUploadController;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 新增用户
     * 支持上传头像文件
     * 只有电话号码是必填的，其他字段可选填
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(
            @RequestPart(value = "user", required = false) String userJson,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 如果没有用户JSON数据，返回错误
            if (userJson == null || userJson.isEmpty()) {
                result.put("code", 400);
                result.put("msg", "用户信息不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            // 解析用户JSON数据
            Users user = objectMapper.readValue(userJson, Users.class);
            
            // 检查电话号码是否存在
            if (user.getPhone() == null || user.getPhone().isEmpty()) {
                result.put("code", 400);
                result.put("msg", "电话号码不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            // 如果上传了头像，处理头像文件
            if (avatar != null && !avatar.isEmpty()) {
                // 使用现有的文件上传控制器上传头像
                ResponseEntity<Map<String, Object>> uploadResult = fileUploadController.uploadFile(avatar);
                if (uploadResult.getStatusCode().is2xxSuccessful() && 
                    uploadResult.getBody() != null && 
                    uploadResult.getBody().containsKey("url")) {
                    // 设置用户头像URL
                    user.setAvatarUrl((String) uploadResult.getBody().get("url"));
                }
            }

            // 设置创建时间和更新时间
            LocalDateTime now = LocalDateTime.now();
            user.setCreatedTime(now);
            user.setUpdatedTime(now);
            user.setStatus("ACTIVE"); // 默认状态为激活

            // 保存用户信息
            boolean saved = usersService.save(user);
            if (saved) {
                result.put("code", 200);
                result.put("msg", "success");
                result.put("data", user);
                return ResponseEntity.ok(result);
            } else {
                result.put("code", 500);
                result.put("msg", "用户创建失败");
                return ResponseEntity.status(500).body(result);
            }

        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "用户创建失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 修改用户
     * 支持上传新的头像文件
     * 通过电话号码识别用户
     */
    @PutMapping
    public ResponseEntity<Map<String, Object>> updateUser(
            @RequestParam("phone") String phone,
            @RequestPart(value = "user", required = false) String userJson,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 检查电话号码是否存在
            if (phone == null || phone.isEmpty()) {
                result.put("code", 400);
                result.put("msg", "电话号码不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 检查用户是否存在
            Users existingUser = usersService.lambdaQuery()
                    .eq(Users::getPhone, phone)
                    .one();
            if (existingUser == null) {
                result.put("code", 404);
                result.put("msg", "用户不存在");
                return ResponseEntity.notFound().build();
            }

            // 如果有用户JSON数据，更新用户信息
            if (userJson != null && !userJson.isEmpty()) {
                // 解析用户JSON数据
                Users updatedUser = objectMapper.readValue(userJson, Users.class);
                // 保留原有ID和时间信息
                updatedUser.setId(existingUser.getId());
                updatedUser.setCreatedTime(existingUser.getCreatedTime());
                updatedUser.setUpdatedTime(LocalDateTime.now());
                // 确保电话号码不变
                updatedUser.setPhone(phone);
                // 如果没有提供头像URL，保留原有头像
                if (!StringUtils.hasText(updatedUser.getAvatarUrl())) {
                    updatedUser.setAvatarUrl(existingUser.getAvatarUrl());
                }
                // 更新用户信息
                existingUser = updatedUser;
            } else {
                // 仅更新时间
                existingUser.setUpdatedTime(LocalDateTime.now());
            }

            // 如果上传了新的头像，处理头像文件
            if (avatar != null && !avatar.isEmpty()) {
                // 使用现有的文件上传控制器上传头像
                ResponseEntity<Map<String, Object>> uploadResult = fileUploadController.uploadFile(avatar);
                if (uploadResult.getStatusCode().is2xxSuccessful() && 
                    uploadResult.getBody() != null && 
                    uploadResult.getBody().containsKey("url")) {
                    // 更新用户头像URL
                    existingUser.setAvatarUrl((String) uploadResult.getBody().get("url"));
                }
            }

            // 保存更新后的用户信息
            boolean updated = usersService.updateById(existingUser);
            if (updated) {
                result.put("code", 200);
                result.put("msg", "success");
                result.put("data", existingUser);
                return ResponseEntity.ok(result);
            } else {
                result.put("code", 500);
                result.put("msg", "用户更新失败");
                return ResponseEntity.status(500).body(result);
            }

        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "用户更新失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
}