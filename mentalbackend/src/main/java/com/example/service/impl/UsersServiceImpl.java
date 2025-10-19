package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.Users;
import com.example.mapper.UsersMapper;
import com.example.service.UsersService;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements UsersService {
    // 可以在这里实现自定义业务方法
}