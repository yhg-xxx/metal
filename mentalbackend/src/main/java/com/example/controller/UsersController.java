package com.example.controller;

import com.example.entity.Users;
import com.example.service.FileUploadService;
import com.example.service.UsersService;
import com.example.utils.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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

    @Resource
    private FileUploadService fileUploadService;

    /**
     * 新增用户
     * 支持上传头像文件
     * 只有电话号码是必填的，其他字段可选填
     */
    @PostMapping
    public Result createUser(
            @RequestPart(value = "user", required = false) String userJson,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) {

        try {
            // 如果没有用户JSON数据，返回错误
            if (userJson == null || userJson.isEmpty()) {
                return Result.error(400, "用户信息不能为空");
            }

            // 解析用户JSON数据
            Users user = objectMapper.readValue(userJson, Users.class);
            
            // 检查电话号码是否存在
            if (user.getPhone() == null || user.getPhone().isEmpty()) {
                return Result.error(400, "电话号码不能为空");
            }

            // 如果上传了头像，处理头像文件
            if (avatar != null && !avatar.isEmpty()) {
                // 使用现有的文件上传控制器上传头像
                Result uploadResult = Result.success(fileUploadController.uploadFile(avatar));
                if (uploadResult.getCode() == 200 && 
                    uploadResult.getData() instanceof Map && 
                    ((Map<?, ?>)uploadResult.getData()).containsKey("url")) {
                    // 设置用户头像URL
                    user.setAvatarUrl((String) ((Map<?, ?>)uploadResult.getData()).get("url"));
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
                return Result.success("success", user);
            } else {
                return Result.error(500, "用户创建失败");
            }

        } catch (Exception e) {
            return Result.error("用户创建失败: " + e.getMessage());
        }
    }

    /**
     * 修改用户
     * 支持上传新的头像文件
     * 通过电话号码识别用户
     */
    @PutMapping
    public Result updateUser(
            @RequestParam("phone") String phone,
            @RequestPart(value = "user", required = false) String userJson,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) {

        try {
            // 检查电话号码是否存在
            if (phone == null || phone.isEmpty()) {
                return Result.error(400, "电话号码不能为空");
            }

            // 检查用户是否存在
            Users existingUser = usersService.lambdaQuery()
                    .eq(Users::getPhone, phone)
                    .one();
            if (existingUser == null) {
                return Result.error(404, "用户不存在");
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
                try {
                    // 直接调用FileUploadService而不是FileUploadController
                    // 这样可以更好地处理返回结果
                    ResponseEntity<Map<String, Object>> uploadResponse = fileUploadService.uploadFile(avatar);

                    if (uploadResponse.getStatusCode().is2xxSuccessful()) {
                        Map<String, Object> uploadResult = uploadResponse.getBody();
                        if (uploadResult != null && uploadResult.containsKey("url")) {
                            // 更新用户头像URL
                            String newAvatarUrl = (String) uploadResult.get("url");
                            existingUser.setAvatarUrl(newAvatarUrl);
                            System.out.println("新头像URL: " + newAvatarUrl); // 添加日志
                        } else {
                            System.err.println("上传成功但返回结果中缺少URL字段");
                        }
                    } else {
                        System.err.println("头像上传失败，状态码: " + uploadResponse.getStatusCode());
                    }
                } catch (Exception e) {
                    System.err.println("头像上传异常: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // 保存更新后的用户信息
            boolean updated = usersService.updateById(existingUser);
            if (updated) {
                return Result.success("success", existingUser);
            } else {
                return Result.error(500, "用户更新失败");
            }

        } catch (Exception e) {
            return Result.error("用户更新失败: " + e.getMessage());
        }
    }
}