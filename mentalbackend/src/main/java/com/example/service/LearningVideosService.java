package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.LearningVideos;

import java.util.List;

/**
 * 学习视频Service
 */
public interface LearningVideosService extends IService<LearningVideos> {
    
    /**
     * 根据学习包ID获取视频列表
     * @param learningPackageId 学习包ID
     * @return 视频列表
     */
    List<LearningVideos> getVideosByPackageId(Long learningPackageId);
    
    /**
     * 根据视频ID获取视频详情
     * @param id 视频ID
     * @return 视频详情
     */
    LearningVideos getVideoById(Long id);
}