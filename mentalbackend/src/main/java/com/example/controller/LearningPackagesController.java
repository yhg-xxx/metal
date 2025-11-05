package com.example.controller;

import com.example.entity.LearningPackages;
import com.example.service.LearningPackagesService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学习包控制器
 */
@RestController
@RequestMapping("/api/learning-packages")
public class LearningPackagesController {

    @Resource
    private LearningPackagesService learningPackagesService;

    @Resource
    private FileUploadController fileUploadController;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 获取所有学习包列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllLearningPackages() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<LearningPackages> learningPackages = learningPackagesService.getAllLearningPackages();
            result.put("code", 200);
            result.put("msg", "success");
            result.put("data", learningPackages);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "获取学习包列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 添加学习包
     * 支持上传封面图片文件
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addLearningPackage(
            @RequestPart(value = "package", required = false) String packageJson,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 检查学习包JSON数据是否存在
            if (packageJson == null || packageJson.isEmpty()) {
                result.put("code", 400);
                result.put("msg", "学习包信息不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            // 解析学习包JSON数据
            LearningPackages learningPackage = objectMapper.readValue(packageJson, LearningPackages.class);
            
            // 检查标题是否存在
            if (learningPackage.getTitle() == null || learningPackage.getTitle().isEmpty()) {
                result.put("code", 400);
                result.put("msg", "学习包标题不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            // 如果上传了封面图片，处理图片文件
            if (coverImage != null && !coverImage.isEmpty()) {
                // 使用现有的文件上传控制器上传封面图片
                ResponseEntity<Map<String, Object>> uploadResult = fileUploadController.uploadFile(coverImage);
                if (uploadResult.getStatusCode().is2xxSuccessful() && 
                    uploadResult.getBody() != null && 
                    uploadResult.getBody().containsKey("url")) {
                    // 设置学习包封面图片URL
                    learningPackage.setCoverImageUrl((String) uploadResult.getBody().get("url"));
                }
            }

            // 保存学习包信息
            boolean saved = learningPackagesService.saveLearningPackage(learningPackage);
            if (saved) {
                result.put("code", 200);
                result.put("msg", "success");
                result.put("data", learningPackage);
                return ResponseEntity.ok(result);
            } else {
                result.put("code", 500);
                result.put("msg", "学习包创建失败");
                return ResponseEntity.status(500).body(result);
            }

        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "学习包创建失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
}