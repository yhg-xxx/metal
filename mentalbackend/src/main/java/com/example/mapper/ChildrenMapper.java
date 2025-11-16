package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Children;
import org.apache.ibatis.annotations.Mapper;

/**
 * 孩子信息Mapper接口
 */
@Mapper
public interface ChildrenMapper extends BaseMapper<Children> {
    // 可以在这里添加自定义查询方法
}