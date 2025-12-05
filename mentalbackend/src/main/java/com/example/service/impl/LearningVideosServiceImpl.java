package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.LearningVideos;
import com.example.mapper.LearningVideosMapper;
import com.example.service.LearningVideosService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.List;

/**
 * 学习视频Service实现类
 */
@Service
@Slf4j
public class LearningVideosServiceImpl extends ServiceImpl<LearningVideosMapper, LearningVideos> implements LearningVideosService {
    
    @Resource
    private LearningVideosMapper learningVideosMapper;
    
    @Override
    public List<LearningVideos> getVideosByPackageId(Long learningPackageId) {
        try {
            log.info("获取学习包ID: {} 的视频列表", learningPackageId);
            List<LearningVideos> videos = learningVideosMapper.getVideosByPackageId(learningPackageId);
            log.info("获取视频列表成功，数量: {}", videos.size());
            return videos;
        } catch (Exception e) {
            log.error("获取学习包视频列表失败: {}, 错误信息: {}", learningPackageId, e.getMessage(), e);
            throw new RuntimeException("获取视频列表失败", e);
        }
    }
    
    @Override
    public LearningVideos getVideoById(Long id) {
        try {
            log.info("获取视频ID: {} 的详情", id);
            LearningVideos video = learningVideosMapper.getVideoById(id);
            if (video == null) {
                log.warn("视频ID: {} 不存在", id);
            } else {
                log.info("获取视频详情成功");
            }
            return video;
        } catch (Exception e) {
            log.error("获取视频详情失败: {}, 错误信息: {}", id, e.getMessage(), e);
            throw new RuntimeException("获取视频详情失败", e);
        }
    }
}