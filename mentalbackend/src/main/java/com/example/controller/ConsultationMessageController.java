package com.example.controller;

import com.example.utils.Result;
import com.example.dto.CounselorDTO;
import com.example.entity.ConsultationMessages;
import com.example.service.ConsultationMessagesService;
import com.example.service.CounselorsService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 咨询消息控制器
 * 提供消息查询相关的REST接口
 */
@RestController
@RequestMapping("/api/consultation/messages")
public class ConsultationMessageController {

    private static final Logger log = LoggerFactory.getLogger(ConsultationMessageController.class);

    @Resource
    private ConsultationMessagesService consultationMessagesService;
    
    @Resource
    private CounselorsService counselorsService;

    /**
     * 根据用户ID和咨询师ID获取对话记录
     * @param userId 用户ID
     * @param counselorId 咨询师ID
     * @param limit 每页数量，默认50
     * @param offset 偏移量，默认0
     * @return 对话消息列表
     */
    @GetMapping("/conversation")
    public ResponseEntity<List<ConsultationMessages>> getConversation(
            @RequestParam Long userId,
            @RequestParam Long counselorId,
            @RequestParam(required = false, defaultValue = "50") Integer limit,
            @RequestParam(required = false, defaultValue = "0") Integer offset) {
        
        log.info("获取对话记录: 用户ID={}, 咨询师ID={}, 限制={}, 偏移={}", 
                userId, counselorId, limit, offset);
        
        try {
            List<ConsultationMessages> messages = consultationMessagesService.getConversationByUserAndCounselor(
                    userId, counselorId, limit, offset);
            
            // 对于咨询师查询时，将用户消息标记为已读
            // 这里可以根据需要添加标记已读的逻辑
            
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("获取对话记录失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 根据预约ID获取消息记录
     * @param appointmentId 预约ID
     * @return 消息列表
     */
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<List<ConsultationMessages>> getMessagesByAppointmentId(
            @PathVariable Long appointmentId) {
        
        log.info("根据预约ID获取消息记录: 预约ID={}", appointmentId);
        
        try {
            List<ConsultationMessages> messages = consultationMessagesService.getMessagesByAppointmentId(appointmentId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("获取预约消息记录失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取用户与每个咨询师的最新一条消息
     * @param userId 用户ID
     * @return 对话消息列表，每个咨询师一条最新消息
     */
    @GetMapping("/user/latest")
    public ResponseEntity<List<ConsultationMessages>> getUserLatestMessagesWithCounselors(
            @RequestParam Long userId) {

        log.info("获取用户与每个咨询师的最新消息: 用户ID={}", userId);

        try {
            List<ConsultationMessages> messages = consultationMessagesService.getUserLatestMessagesWithCounselors(userId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("获取用户最新消息失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取用户所有进行过对话的咨询师信息
     * @param userId 用户ID
     * @return Result<List<CounselorDTO>> 咨询师信息列表
     */
    @GetMapping("/user/counselors")
    public Result getUserConversatedCounselors(@RequestParam("userId") Long userId) {
        try {
            log.info("获取用户所有进行过对话的咨询师信息: 用户ID={}", userId);
            
            if (userId == null) {
                log.error("用户ID不能为空");
                return Result.error(400, "用户ID不能为空");
            }
            
            // 获取用户与每个咨询师的最新消息
            List<ConsultationMessages> latestMessages = consultationMessagesService.getUserLatestMessagesWithCounselors(userId);
            if (latestMessages == null || latestMessages.isEmpty()) {
                log.info("用户还没有任何对话记录");
                return Result.success("查询成功", new ArrayList<CounselorDTO>());
            }
            
            // 提取咨询师ID并去重
            Set<Long> counselorIdSet = new HashSet<>();
            for (ConsultationMessages message : latestMessages) {
                if (message.getCounselorId() != null) {
                    counselorIdSet.add(message.getCounselorId());
                }
            }
            
            // 获取每个咨询师的详情
            List<CounselorDTO> counselorDTOList = new ArrayList<>();
            for (Long counselorId : counselorIdSet) {
                CounselorDTO counselorDTO = counselorsService.getCounselorDetail(counselorId);
                if (counselorDTO != null) {
                    counselorDTOList.add(counselorDTO);
                }
            }
            
            log.info("获取用户对话咨询师信息成功，共{}名咨询师", counselorDTOList.size());
            return Result.success("查询成功", counselorDTOList);
        } catch (Exception e) {
            log.error("获取用户对话咨询师信息失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建初始对话
     * @param userId 用户ID
     * @param counselorId 咨询师ID
     * @return Result<Boolean>
     */
    @PostMapping("/initial")
    public Result createInitialConversation(@RequestParam Long userId, @RequestParam Long counselorId) {
        try {
            log.info("创建初始对话，userId: {}, counselorId: {}", userId, counselorId);
            
            // 参数校验
            if (userId == null || counselorId == null) {
                log.error("参数错误：用户ID或咨询师ID不能为空");
                return Result.error("参数错误：用户ID或咨询师ID不能为空");
            }
            
            // 获取咨询师详情
            CounselorDTO counselorDTO = counselorsService.getCounselorDetail(counselorId);
            if (counselorDTO == null) {
                log.error("咨询师不存在，counselorId: {}", counselorId);
                return Result.error("咨询师不存在");
            }
            
            // 构建问候消息内容
            StringBuilder greetingContent = new StringBuilder();
            greetingContent.append("你好！我是")
                           .append(counselorDTO.getRealName() != null ? counselorDTO.getRealName() : "心理咨询师")
                           .append("。");
              
            // 添加咨询师介绍
            if (counselorDTO.getIntroduction() != null && !counselorDTO.getIntroduction().isEmpty()) {
                greetingContent.append(" ")
                              .append(counselorDTO.getIntroduction())
                              .append(" 很高兴为您提供心理咨询服务，请问有什么可以帮助您的吗？");
            } else {
                greetingContent.append(" 很高兴为您提供心理咨询服务，请问有什么可以帮助您的吗？");
            }
            
            // 创建消息对象
            ConsultationMessages message = new ConsultationMessages();
            message.setUserId(userId);
            message.setCounselorId(counselorId);
            message.setSenderType("COUNSELOR");
            message.setMessageType("TEXT");
            message.setContent(greetingContent.toString());
            message.setReadStatus(false);
            message.setSentTime(LocalDateTime.now());
            
            // 保存消息
            boolean result = consultationMessagesService.saveMessage(message);
            if (result) {
                log.info("初始对话创建成功，userId: {}, counselorId: {}", userId, counselorId);
                return Result.success(true);
            } else {
                log.error("初始对话创建失败，保存消息失败");
                return Result.error("初始对话创建失败");
            }
        } catch (Exception e) {
            log.error("创建初始对话失败，userId: {}, counselorId: {}, error: {}", userId, counselorId, e.getMessage());
            return Result.error("创建初始对话失败");
        }
    }
}