package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.TestQuestions;

import java.util.List;

/**
 * 测试题库表服务接口
 */
public interface TestQuestionsService extends IService<TestQuestions> {
    /**
     * 根据学习包ID获取题目列表
     * @param learningPackageId 学习包ID
     * @return 题目列表
     */
    List<TestQuestions> getQuestionsByLearningPackageId(Long learningPackageId);
}
