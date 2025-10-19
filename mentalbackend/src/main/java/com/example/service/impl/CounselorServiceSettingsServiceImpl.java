package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.CounselorServiceSettings;
import com.example.mapper.CounselorServiceSettingsMapper;
import com.example.service.CounselorServiceSettingsService;
import org.springframework.stereotype.Service;

/**
 * 咨询师服务设置服务实现类
 */
@Service
public class CounselorServiceSettingsServiceImpl extends ServiceImpl<CounselorServiceSettingsMapper, CounselorServiceSettings> implements CounselorServiceSettingsService {
    // 可以在这里实现自定义业务方法
}