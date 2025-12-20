package com.example.controller;

import com.example.entity.PsychologicalAssessments;
import com.example.entity.TestAnswers;
import com.example.entity.TestQuestions;
import com.example.entity.TestRecords;
import com.example.service.PsychologicalAssessmentsService;
import com.example.service.TestAnswersService;
import com.example.service.TestQuestionsService;
import com.example.service.TestRecordsService;
import com.example.utils.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 心理测评控制器
 */
@RestController
@RequestMapping("/api/tests")
public class TestController {

    @Resource
    private TestQuestionsService testQuestionsService;

    @Resource
    private TestRecordsService testRecordsService;

    @Resource
    private TestAnswersService testAnswersService;

    @Resource
    private PsychologicalAssessmentsService psychologicalAssessmentsService;

    /**
     * 根据学习包ID获取测试题目
     * @param learningPackageId 学习包ID
     * @return 测试题目列表
     */
    @GetMapping("/questions")
    public Result getQuestionsByLearningPackageId(@RequestParam Long learningPackageId) {
        try {
            if (learningPackageId == null) {
                return Result.error(400, "学习包ID不能为空");
            }
            List<TestQuestions> questions = testQuestionsService.getQuestionsByLearningPackageId(learningPackageId);
            return Result.success(questions);
        } catch (Exception e) {
            return Result.error(500, "获取测试题目失败: " + e.getMessage());
        }
    }

    /**
     * 创建测试记录
     * @param userId 用户ID
     * @param learningPackageId 学习包ID
     * @param totalQuestions 总题目数
     * @return 测试记录
     */
    @PostMapping("/records")
    public Result createTestRecord(@RequestParam Long userId, @RequestParam Long learningPackageId, @RequestParam Integer totalQuestions) {
        try {
            if (userId == null || learningPackageId == null || totalQuestions == null) {
                return Result.error(400, "参数不能为空");
            }
            TestRecords testRecord = testRecordsService.createTestRecord(userId, learningPackageId, totalQuestions);
            if (testRecord == null) {
                return Result.error(500, "创建测试记录失败");
            }
            return Result.success(testRecord);
        } catch (Exception e) {
            return Result.error(500, "创建测试记录失败: " + e.getMessage());
        }
    }

    /**
     * 保存用户答案
     * @param testAnswer 用户答案
     * @return 保存结果
     */
    @PostMapping("/answers")
    public Result saveTestAnswer(@RequestBody TestAnswers testAnswer) {
        try {
            if (testAnswer == null || testAnswer.getTestRecordId() == null || testAnswer.getQuestionId() == null) {
                return Result.error(400, "参数不能为空");
            }
            boolean result = testAnswersService.saveTestAnswer(testAnswer);
            if (result) {
                return Result.success("保存答案成功");
            } else {
                return Result.error(500, "保存答案失败");
            }
        } catch (Exception e) {
            return Result.error(500, "保存答案失败: " + e.getMessage());
        }
    }

    /**
     * 完成测试
     * @param testRecordId 测试记录ID
     * @param correctAnswers 正确答案数
     * @param score 得分
     * @param timeSpentSeconds 用时（秒）
     * @return 测试记录
     */
    @PutMapping("/records/{testRecordId}/complete")
    public Result completeTest(@PathVariable Long testRecordId, @RequestParam Integer correctAnswers, @RequestParam Integer score, @RequestParam Integer timeSpentSeconds) {
        try {
            if (testRecordId == null) {
                return Result.error(400, "测试记录ID不能为空");
            }
            TestRecords testRecord = testRecordsService.completeTest(testRecordId, correctAnswers, score, timeSpentSeconds);
            if (testRecord == null) {
                return Result.error(500, "完成测试失败");
            }
            return Result.success(testRecord);
        } catch (Exception e) {
            return Result.error(500, "完成测试失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户测试记录
     * @param userId 用户ID
     * @return 测试记录列表
     */
    @GetMapping("/records/user/{userId}")
    public Result getUserTestRecords(@PathVariable Long userId) {
        try {
            if (userId == null) {
                return Result.error(400, "用户ID不能为空");
            }
            List<TestRecords> testRecords = testRecordsService.getUserTestRecords(userId);
            return Result.success(testRecords);
        } catch (Exception e) {
            return Result.error(500, "获取用户测试记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取测试记录详情
     * @param testRecordId 测试记录ID
     * @return 测试记录
     */
    @GetMapping("/records/{testRecordId}")
    public Result getTestRecordDetail(@PathVariable Long testRecordId) {
        try {
            if (testRecordId == null) {
                return Result.error(400, "测试记录ID不能为空");
            }
            TestRecords testRecord = testRecordsService.getTestRecordDetail(testRecordId);
            if (testRecord == null) {
                return Result.error(404, "测试记录不存在");
            }
            return Result.success(testRecord);
        } catch (Exception e) {
            return Result.error(500, "获取测试记录详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据测试记录ID获取答题详情
     * @param testRecordId 测试记录ID
     * @return 答题详情列表
     */
    @GetMapping("/answers/{testRecordId}")
    public Result getAnswersByTestRecordId(@PathVariable Long testRecordId) {
        try {
            if (testRecordId == null) {
                return Result.error(400, "测试记录ID不能为空");
            }
            List<TestAnswers> testAnswers = testAnswersService.getAnswersByTestRecordId(testRecordId);
            return Result.success(testAnswers);
        } catch (Exception e) {
            return Result.error(500, "获取答题详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据测试记录ID获取评估报告
     * @param testRecordId 测试记录ID
     * @return 评估报告
     */
    @GetMapping("/assessments/{testRecordId}")
    public Result getAssessmentByTestRecordId(@PathVariable Long testRecordId) {
        try {
            if (testRecordId == null) {
                return Result.error(400, "测试记录ID不能为空");
            }
            PsychologicalAssessments assessment = psychologicalAssessmentsService.getAssessmentByTestRecordId(testRecordId);
            if (assessment == null) {
                return Result.error(404, "评估报告不存在");
            }
            return Result.success(assessment);
        } catch (Exception e) {
            return Result.error(500, "获取评估报告失败: " + e.getMessage());
        }
    }

    /**
     * 生成评估报告
     * @param assessment 评估报告信息
     * @return 生成的评估报告
     */
    @PostMapping("/assessments")
    public Result generateAssessment(@RequestBody PsychologicalAssessments assessment) {
        try {
            if (assessment == null || assessment.getUserId() == null || assessment.getTestRecordId() == null) {
                return Result.error(400, "参数不能为空");
            }
            PsychologicalAssessments generatedAssessment = psychologicalAssessmentsService.generateAssessment(assessment);
            if (generatedAssessment == null) {
                return Result.error(500, "生成评估报告失败");
            }
            return Result.success(generatedAssessment);
        } catch (Exception e) {
            return Result.error(500, "生成评估报告失败: " + e.getMessage());
        }
    }
}

