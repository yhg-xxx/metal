package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.PsychologicalAssessments;

/**
 * 心理评估报告表服务接口
 */
public interface PsychologicalAssessmentsService extends IService<PsychologicalAssessments> {
    /**
     * 根据测试记录ID获取评估报告
     * @param testRecordId 测试记录ID
     * @return 评估报告
     */
    PsychologicalAssessments getAssessmentByTestRecordId(Long testRecordId);

    /**
     * 生成评估报告
     * @param assessment 评估报告信息
     * @return 生成的评估报告
     */
    PsychologicalAssessments generateAssessment(PsychologicalAssessments assessment);
}
