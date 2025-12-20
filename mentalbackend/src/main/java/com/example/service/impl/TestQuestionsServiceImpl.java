package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.TestQuestions;
import com.example.mapper.TestQuestionsMapper;
import com.example.service.TestQuestionsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 测试题库表服务实现类
 */
@Service
@Slf4j
public class TestQuestionsServiceImpl extends ServiceImpl<TestQuestionsMapper, TestQuestions> implements TestQuestionsService {

    @Resource
    private TestQuestionsMapper testQuestionsMapper;

    @Override
    public List<TestQuestions> getQuestionsByLearningPackageId(Long learningPackageId) {
        if (learningPackageId == null) {
            log.error("获取题目失败：学习包ID为空");
            return null;
        }
        try {
            QueryWrapper<TestQuestions> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("learning_package_id", learningPackageId)
                    .eq("status", "ACTIVE")
                    .orderByAsc("sort_order");
            return testQuestionsMapper.selectList(queryWrapper);
        } catch (Exception e) {
            log.error("获取题目失败：learningPackageId={}, error={}", learningPackageId, e.getMessage());
            return null;
        }
    }
}
