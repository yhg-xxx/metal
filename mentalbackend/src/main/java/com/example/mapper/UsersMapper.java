package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Users;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户信息Mapper接口
 */
@Mapper
public interface UsersMapper extends BaseMapper<Users> {
    // 可以在这里添加自定义查询方法
}