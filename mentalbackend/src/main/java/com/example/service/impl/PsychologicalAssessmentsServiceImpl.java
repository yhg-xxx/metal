package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.PsychologicalAssessments;
import com.example.mapper.PsychologicalAssessmentsMapper;
import com.example.service.PsychologicalAssessmentsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 心理评估报告表服务实现类
 */
@Service
@Slf4j
public class PsychologicalAssessmentsServiceImpl extends ServiceImpl<PsychologicalAssessmentsMapper, PsychologicalAssessments> implements PsychologicalAssessmentsService {

    @Resource
    private PsychologicalAssessmentsMapper psychologicalAssessmentsMapper;

    @Override
    public PsychologicalAssessments getAssessmentByTestRecordId(Long testRecordId) {
        if (testRecordId == null) {
            log.error("获取评估报告失败：测试记录ID为空");
            return null;
        }
        try {
            QueryWrapper<PsychologicalAssessments> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("test_record_id", testRecordId);
            return psychologicalAssessmentsMapper.selectOne(queryWrapper);
        } catch (Exception e) {
            log.error("获取评估报告失败：testRecordId={}, error={}", testRecordId, e.getMessage());
            return null;
        }
    }

    @Override
    public PsychologicalAssessments generateAssessment(PsychologicalAssessments assessment) {
        if (assessment == null || assessment.getUserId() == null || assessment.getTestRecordId() == null) {
            log.error("生成评估报告失败：参数为空");
            return null;
        }
        try {
            assessment.setAssessmentDate(LocalDate.now());
            assessment.setCreatedTime(LocalDateTime.now());
            psychologicalAssessmentsMapper.insert(assessment);
            log.info("生成评估报告成功：userId={}, testRecordId={}", assessment.getUserId(), assessment.getTestRecordId());
            return assessment;
        } catch (Exception e) {
            log.error("生成评估报告失败：userId={}, testRecordId={}, error={}", assessment.getUserId(), assessment.getTestRecordId(), e.getMessage());
            return null;
        }
    }
}
