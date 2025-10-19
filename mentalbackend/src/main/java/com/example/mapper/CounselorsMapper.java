package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Counselors;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 咨询师信息Mapper接口
 */
@Mapper
public interface CounselorsMapper extends BaseMapper<Counselors> {
    // 根据关键词搜索咨询师
    List<Counselors> searchByKeyword(@Param("keyword") String keyword);
    
    // 根据标签筛选咨询师
    List<Counselors> filterByTags(
            @Param("specializationTags") List<String> specializationTags,
            @Param("approachTags") List<String> approachTags,
            @Param("serviceTypeTags") List<String> serviceTypeTags,
            @Param("gender") String gender
    );
}