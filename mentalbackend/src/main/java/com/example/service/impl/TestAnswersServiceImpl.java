package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.TestAnswers;
import com.example.mapper.TestAnswersMapper;
import com.example.service.TestAnswersService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 测试答题详情表服务实现类
 */
@Service
@Slf4j
public class TestAnswersServiceImpl extends ServiceImpl<TestAnswersMapper, TestAnswers> implements TestAnswersService {

    @Resource
    private TestAnswersMapper testAnswersMapper;

    @Override
    public boolean saveTestAnswer(TestAnswers testAnswer) {
        if (testAnswer == null || testAnswer.getTestRecordId() == null || testAnswer.getQuestionId() == null) {
            log.error("保存用户答案失败：参数为空");
            return false;
        }
        try {
            testAnswersMapper.insert(testAnswer);
            log.info("保存用户答案成功：testRecordId={}, questionId={}", testAnswer.getTestRecordId(), testAnswer.getQuestionId());
            return true;
        } catch (Exception e) {
            log.error("保存用户答案失败：testRecordId={}, questionId={}, error={}", testAnswer.getTestRecordId(), testAnswer.getQuestionId(), e.getMessage());
            return false;
        }
    }

    @Override
    public List<TestAnswers> getAnswersByTestRecordId(Long testRecordId) {
        if (testRecordId == null) {
            log.error("获取答题详情失败：测试记录ID为空");
            return null;
        }
        try {
            QueryWrapper<TestAnswers> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("test_record_id", testRecordId);
            return testAnswersMapper.selectList(queryWrapper);
        } catch (Exception e) {
            log.error("获取答题详情失败：testRecordId={}, error={}", testRecordId, e.getMessage());
            return null;
        }
    }
}
