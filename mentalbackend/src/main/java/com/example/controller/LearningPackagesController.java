package com.example.controller;

import com.example.entity.LearningPackages;
import com.example.service.LearningPackagesService;
import com.example.utils.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public Result getAllLearningPackages() {
        try {
            List<LearningPackages> learningPackages = learningPackagesService.getAllLearningPackages();
            return Result.success("success", learningPackages);
        } catch (Exception e) {
            return Result.error(500, "获取学习包列表失败: " + e.getMessage());
        }
    }

    /**
     * 添加学习包
     * 支持上传封面图片文件
     */
    @PostMapping
    public Result addLearningPackage(
            @RequestPart(value = "package", required = false) String packageJson,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage) {

        try {
            // 检查学习包JSON数据是否存在
            if (packageJson == null || packageJson.isEmpty()) {
                return Result.error(400, "学习包信息不能为空");
            }

            // 解析学习包JSON数据
            LearningPackages learningPackage = objectMapper.readValue(packageJson, LearningPackages.class);
            
            // 检查标题是否存在
            if (learningPackage.getTitle() == null || learningPackage.getTitle().isEmpty()) {
                return Result.error(400, "学习包标题不能为空");
            }

            // 如果上传了封面图片，处理图片文件
            if (coverImage != null && !coverImage.isEmpty()) {
                // 使用现有的文件上传控制器上传封面图片
                Result uploadResult = Result.success(fileUploadController.uploadFile(coverImage));
                if (uploadResult.getCode() == 200 && 
                    uploadResult.getData() instanceof Map &&
                    ((Map<?, ?>)uploadResult.getData()).containsKey("url")) {
                    // 设置学习包封面图片URL
                    learningPackage.setCoverImageUrl((String) ((Map<?, ?>)uploadResult.getData()).get("url"));
                }
            }

            // 保存学习包信息
            boolean saved = learningPackagesService.saveLearningPackage(learningPackage);
            if (saved) {
                return Result.success("success", learningPackage);
            } else {
                return Result.error(500, "学习包创建失败");
            }

        } catch (Exception e) {
            return Result.error(500, "学习包创建失败: " + e.getMessage());
        }
    }
}