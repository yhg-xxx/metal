package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.LearningPackages;
import java.util.List;

/**
 * 学习包服务接口
 */
public interface LearningPackagesService extends IService<LearningPackages> {
    /**
     * 获取所有学习包列表
     * @return 学习包列表
     */
    List<LearningPackages> getAllLearningPackages();
    
    /**
     * 根据ID获取学习包详情
     * @param id 学习包ID
     * @return 学习包信息
     */
    LearningPackages getLearningPackageById(Long id);
    
    /**
     * 保存学习包信息
     * @param learningPackage 学习包信息
     * @return 是否保存成功
     */
    boolean saveLearningPackage(LearningPackages learningPackage);
}