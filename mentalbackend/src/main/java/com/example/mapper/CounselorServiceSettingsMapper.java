package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.CounselorServiceSettings;
import org.apache.ibatis.annotations.Mapper;

/**
 * 咨询师服务设置Mapper接口
 */
@Mapper
public interface CounselorServiceSettingsMapper extends BaseMapper<CounselorServiceSettings> {
    // 可以在这里添加自定义查询方法
}