package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.TestAnswers;

import java.util.List;

/**
 * 测试答题详情表服务接口
 */
public interface TestAnswersService extends IService<TestAnswers> {
    /**
     * 保存用户答案
     * @param testAnswer 用户答案
     * @return 保存结果
     */
    boolean saveTestAnswer(TestAnswers testAnswer);

    /**
     * 根据测试记录ID获取答题详情
     * @param testRecordId 测试记录ID
     * @return 答题详情列表
     */
    List<TestAnswers> getAnswersByTestRecordId(Long testRecordId);
}
