package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.LearningVideos;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 学习视频Mapper
 */
@Mapper
public interface LearningVideosMapper extends BaseMapper<LearningVideos> {
    
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