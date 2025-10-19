package com.example.controller;

import com.example.entity.ConsultationMessages;
import com.example.service.ConsultationMessagesService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}