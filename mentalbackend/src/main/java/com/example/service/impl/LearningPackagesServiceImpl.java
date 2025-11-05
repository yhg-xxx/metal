package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.LearningPackages;
import com.example.mapper.LearningPackagesMapper;
import com.example.service.LearningPackagesService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学习包服务实现类
 */
@Service
public class LearningPackagesServiceImpl extends ServiceImpl<LearningPackagesMapper, LearningPackages> implements LearningPackagesService {

    private static final Logger log = LoggerFactory.getLogger(LearningPackagesServiceImpl.class);

    @Resource
    private LearningPackagesMapper learningPackagesMapper;

    @Override
    public List<LearningPackages> getAllLearningPackages() {
        try {
            return learningPackagesMapper.selectList(null);
        } catch (Exception e) {
            log.error("获取学习包列表失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public LearningPackages getLearningPackageById(Long id) {
        try {
            return learningPackagesMapper.selectById(id);
        } catch (Exception e) {
            log.error("根据ID获取学习包详情失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean saveLearningPackage(LearningPackages learningPackage) {
        try {
            // 设置创建时间和更新时间
            LocalDateTime now = LocalDateTime.now();
            if (learningPackage.getId() == null) {
                // 新增时设置创建时间
                learningPackage.setCreatedTime(now);
            }
            // 无论新增还是更新都设置更新时间
            learningPackage.setUpdatedTime(now);
            
            // 设置默认值
            if (learningPackage.getVideoCount() == null) {
                learningPackage.setVideoCount(0);
            }
            if (learningPackage.getEstimatedDurationMinutes() == null) {
                learningPackage.setEstimatedDurationMinutes(0);
            }
            if (learningPackage.getDifficultyLevel() == null) {
                learningPackage.setDifficultyLevel("BEGINNER");
            }
            if (learningPackage.getStatus() == null) {
                learningPackage.setStatus("DRAFT");
            }
            
            return this.saveOrUpdate(learningPackage);
        } catch (Exception e) {
            log.error("保存学习包信息失败: {}", e.getMessage(), e);
            throw e;
        }
    }
}