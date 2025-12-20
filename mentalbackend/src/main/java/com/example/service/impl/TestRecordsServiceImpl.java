package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.TestRecords;
import com.example.mapper.TestRecordsMapper;
import com.example.service.TestRecordsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 测试记录表服务实现类
 */
@Service
@Slf4j
public class TestRecordsServiceImpl extends ServiceImpl<TestRecordsMapper, TestRecords> implements TestRecordsService {

    @Resource
    private TestRecordsMapper testRecordsMapper;

    @Override
    public TestRecords createTestRecord(Long userId, Long learningPackageId, Integer totalQuestions) {
        if (userId == null || learningPackageId == null || totalQuestions == null) {
            log.error("创建测试记录失败：参数为空");
            return null;
        }
        try {
            TestRecords testRecord = new TestRecords();
            testRecord.setUserId(userId);
            testRecord.setLearningPackageId(learningPackageId);
            testRecord.setTotalQuestions(totalQuestions);
            testRecord.setAnsweredQuestions(0);
            testRecord.setCorrectAnswers(0);
            testRecord.setScore(BigDecimal.ZERO);
            testRecord.setTimeSpentSeconds(0);
            testRecord.setTimeLimitSeconds(900); // 默认15分钟
            testRecord.setStatus("IN_PROGRESS");
            testRecord.setStartedTime(LocalDateTime.now());
            testRecordsMapper.insert(testRecord);
            log.info("创建测试记录成功：userId={}, learningPackageId={}, testRecordId={}", userId, learningPackageId, testRecord.getId());
            return testRecord;
        } catch (Exception e) {
            log.error("创建测试记录失败：userId={}, learningPackageId={}, error={}", userId, learningPackageId, e.getMessage());
            return null;
        }
    }

    @Override
    public TestRecords completeTest(Long testRecordId, Integer correctAnswers, Integer score, Integer timeSpentSeconds) {
        if (testRecordId == null) {
            log.error("完成测试失败：测试记录ID为空");
            return null;
        }
        try {
            TestRecords testRecord = testRecordsMapper.selectById(testRecordId);
            if (testRecord == null) {
                log.error("完成测试失败：测试记录不存在，testRecordId={}", testRecordId);
                return null;
            }
            testRecord.setCorrectAnswers(correctAnswers);
            testRecord.setScore(BigDecimal.valueOf(score));
            testRecord.setTimeSpentSeconds(timeSpentSeconds);
            testRecord.setStatus("COMPLETED");
            testRecord.setSubmittedTime(LocalDateTime.now());
            testRecordsMapper.updateById(testRecord);
            log.info("完成测试成功：testRecordId={}, score={}", testRecordId, score);
            return testRecord;
        } catch (Exception e) {
            log.error("完成测试失败：testRecordId={}, error={}", testRecordId, e.getMessage());
            return null;
        }
    }

    @Override
    public List<TestRecords> getUserTestRecords(Long userId) {
        if (userId == null) {
            log.error("获取用户测试记录失败：用户ID为空");
            return null;
        }
        try {
            QueryWrapper<TestRecords> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .orderByDesc("started_time");
            return testRecordsMapper.selectList(queryWrapper);
        } catch (Exception e) {
            log.error("获取用户测试记录失败：userId={}, error={}", userId, e.getMessage());
            return null;
        }
    }

    @Override
    public TestRecords getTestRecordDetail(Long testRecordId) {
        if (testRecordId == null) {
            log.error("获取测试记录详情失败：测试记录ID为空");
            return null;
        }
        try {
            return testRecordsMapper.selectById(testRecordId);
        } catch (Exception e) {
            log.error("获取测试记录详情失败：testRecordId={}, error={}", testRecordId, e.getMessage());
            return null;
        }
    }
}
