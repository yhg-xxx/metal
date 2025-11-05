package com.example.controller;

import com.example.entity.LearningVideos;
import com.example.service.LearningVideosService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学习视频Controller
 */
@RestController
@RequestMapping("/api/learning-videos")
public class LearningVideosController {
    
    private static final Logger logger = LoggerFactory.getLogger(LearningVideosController.class);
    
    @Resource
    private LearningVideosService learningVideosService;
    
    /**
     * 根据学习包ID获取视频列表
     * @param learningPackageId 学习包ID
     * @return 响应结果
     */
    @GetMapping("/package/{learningPackageId}")
    public Map<String, Object> getVideosByPackageId(@PathVariable Long learningPackageId) {
        Map<String, Object> result = new HashMap<>();
        try {
            logger.info("接收获取学习包视频列表请求，学习包ID: {}", learningPackageId);
            List<LearningVideos> videos = learningVideosService.getVideosByPackageId(learningPackageId);
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", videos);
        } catch (Exception e) {
            logger.error("获取学习包视频列表异常: {}", e.getMessage(), e);
            result.put("code", 500);
            result.put("message", "获取失败: " + e.getMessage());
        }
        return result;
    }
    
    /**
     * 根据视频ID获取视频详情
     * @param id 视频ID
     * @return 响应结果
     */
    @GetMapping("/{id}")
    public Map<String, Object> getVideoById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            logger.info("接收获取视频详情请求，视频ID: {}", id);
            LearningVideos video = learningVideosService.getVideoById(id);
            if (video == null) {
                result.put("code", 404);
                result.put("message", "视频不存在");
            } else {
                result.put("code", 200);
                result.put("message", "获取成功");
                result.put("data", video);
            }
        } catch (Exception e) {
            logger.error("获取视频详情异常: {}", e.getMessage(), e);
            result.put("code", 500);
            result.put("message", "获取失败: " + e.getMessage());
        }
        return result;
    }
}