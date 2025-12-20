package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.TestRecords;

import java.util.List;

/**
 * 测试记录表服务接口
 */
public interface TestRecordsService extends IService<TestRecords> {
    /**
     * 创建测试记录
     * @param userId 用户ID
     * @param learningPackageId 学习包ID
     * @param totalQuestions 总题目数
     * @return 测试记录
     */
    TestRecords createTestRecord(Long userId, Long learningPackageId, Integer totalQuestions);

    /**
     * 完成测试
     * @param testRecordId 测试记录ID
     * @param correctAnswers 正确答案数
     * @param score 得分
     * @param timeSpentSeconds 用时（秒）
     * @return 测试记录
     */
    TestRecords completeTest(Long testRecordId, Integer correctAnswers, Integer score, Integer timeSpentSeconds);

    /**
     * 获取用户测试记录
     * @param userId 用户ID
     * @return 测试记录列表
     */
    List<TestRecords> getUserTestRecords(Long userId);

    /**
     * 获取测试记录详情
     * @param testRecordId 测试记录ID
     * @return 测试记录
     */
    TestRecords getTestRecordDetail(Long testRecordId);
}
