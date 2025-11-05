package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.LearningVideos;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
    @Select("SELECT * FROM learning_videos WHERE learning_package_id = #{learningPackageId} ORDER BY sort_order ASC, created_time DESC")
    List<LearningVideos> getVideosByPackageId(Long learningPackageId);
    
    /**
     * 根据视频ID获取视频详情
     * @param id 视频ID
     * @return 视频详情
     */
    @Select("SELECT * FROM learning_videos WHERE id = #{id}")
    LearningVideos getVideoById(Long id);
}