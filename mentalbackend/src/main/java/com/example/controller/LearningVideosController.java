package com.example.controller;

import com.example.entity.LearningVideos;
import com.example.service.LearningVideosService;
import com.example.utils.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学习视频Controller
 */
@RestController
@RequestMapping("/api/learning-videos")
@Slf4j
public class LearningVideosController {
    
    @Resource
    private LearningVideosService learningVideosService;
    
    /**
     * 根据学习包ID获取视频列表
     * @param learningPackageId 学习包ID
     * @return 响应结果
     */
    @GetMapping("/package/{learningPackageId}")
    public Result getVideosByPackageId(@PathVariable Long learningPackageId) {
        try {
            log.info("接收获取学习包视频列表请求，学习包ID: {}", learningPackageId);
            List<LearningVideos> videos = learningVideosService.getVideosByPackageId(learningPackageId);
            return Result.success("获取成功", videos);
        } catch (Exception e) {
            log.error("获取学习包视频列表异常: {}", e.getMessage(), e);
            return Result.error(500, "获取失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据视频ID获取视频详情
     * @param id 视频ID
     * @return 响应结果
     */
    @GetMapping("/{id}")
    public Result getVideoById(@PathVariable Long id) {
        try {
            log.info("接收获取视频详情请求，视频ID: {}", id);
            LearningVideos video = learningVideosService.getVideoById(id);
            if (video == null) {
                return Result.error(404, "视频不存在");
            } else {
                return Result.success("获取成功", video);
            }
        } catch (Exception e) {
            log.error("获取视频详情异常: {}", e.getMessage(), e);
            return Result.error(500, "获取失败: " + e.getMessage());
        }
    }
}