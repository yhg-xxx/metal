package com.example.controller;

import com.example.entity.ConsultationMessages;
import com.example.service.ConsultationMessagesService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

/**
 * WebSocket消息控制器 - 简化版
 * 核心功能：
 * 1. 处理私聊消息路由
 * 2. 消息格式转换和验证
 * 3. 错误处理和状态管理
 */
@Controller
public class WebSocketController {

    private static final Logger log = LoggerFactory.getLogger(WebSocketController.class);

    @Resource
    private SimpMessagingTemplate messagingTemplate;
    
    @Resource
    private ConsultationMessagesService consultationMessagesService;

    /**
     * 发送私聊消息（简化版，只保留实时对话功能）
     * 消息路由逻辑：
     * 1. 用户发送消息到 /app/chat.private
     * 2. 服务器根据接收者类型和ID路由消息
     * 3. 同时发送确认消息给发送者
     */
    @MessageMapping("/chat.private")
    public void handlePrivateMessage(@Payload MessageDTO messageDTO) {
        try {
            log.info("收到私聊消息: 发送者={}({}), 接收者={}, 内容={}",
                    messageDTO.getSenderId(), messageDTO.getSenderType(),
                    messageDTO.getReceiverId(), messageDTO.getContent());

            // 创建简单的消息对象用于传输
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSenderId(messageDTO.getSenderId());
            chatMessage.setReceiverId(messageDTO.getReceiverId());
            chatMessage.setSenderType(messageDTO.getSenderType());
            chatMessage.setContent(messageDTO.getContent());
            chatMessage.setTimestamp(LocalDateTime.now());
            
            // 保存消息到数据库
            ConsultationMessages consultationMessage = new ConsultationMessages();
            consultationMessage.setSenderType(messageDTO.getSenderType());
            consultationMessage.setMessageType("TEXT");
            consultationMessage.setContent(messageDTO.getContent());
            consultationMessage.setSentTime(LocalDateTime.now());
            consultationMessage.setReadStatus(false);
            consultationMessage.setConversationType("PRE_CONSULTATION"); // 默认咨询前
            
            // 根据发送者类型设置用户ID和咨询师ID
            if ("USER".equals(messageDTO.getSenderType())) {
                consultationMessage.setUserId(messageDTO.getSenderId());
                consultationMessage.setCounselorId(messageDTO.getReceiverId());
            } else {
                consultationMessage.setUserId(messageDTO.getReceiverId());
                consultationMessage.setCounselorId(messageDTO.getSenderId());
            }
            
            // 保存消息
            boolean saved = consultationMessagesService.saveMessage(consultationMessage);
            if (!saved) {
                log.warn("消息保存失败: 发送者ID={}, 接收者ID={}", messageDTO.getSenderId(), messageDTO.getReceiverId());
                // 即使保存失败，仍然继续发送消息，确保实时性
            } else {
                log.info("消息已保存到数据库，消息ID={}", consultationMessage.getId());
            }

            // 发送消息给接收者
            // 根据发送者类型确定接收者类型（用户↔咨询师）
            String receiverType = "USER".equals(messageDTO.getSenderType()) ? "COUNSELOR" : "USER";
            String destination = "/queue/messages/" + receiverType.toLowerCase() + "/" + messageDTO.getReceiverId();

            messagingTemplate.convertAndSend(destination, chatMessage);
            log.info("消息路由到: {}", destination);

            // 发送确认消息给发送者（可选，用于前端确认）
            String senderDestination = "/queue/messages/" + messageDTO.getSenderType().toLowerCase() + "/" + messageDTO.getSenderId();
            messagingTemplate.convertAndSend(senderDestination, chatMessage);

            log.info("消息已发送并确认: 发送者ID={}, 接收者ID={}", messageDTO.getSenderId(), messageDTO.getReceiverId());
        } catch (Exception e) {
            log.error("处理私聊消息异常: {}", e.getMessage(), e);
            // 发送错误消息给发送者
            ErrorDTO errorDTO = new ErrorDTO("系统错误，请稍后重试");
            messagingTemplate.convertAndSend("/queue/errors/" + messageDTO.getSenderType().toLowerCase() + "/" + messageDTO.getSenderId(), errorDTO);
        }
    }

    /**
     * 消息DTO类，用于接收客户端发送的消息
     */
    public static class MessageDTO {
        private Long senderId;
        private Long receiverId;
        private String senderType; // USER, COUNSELOR
        private String content;

        // Getters and Setters
        public Long getSenderId() {
            return senderId;
        }

        public void setSenderId(Long senderId) {
            this.senderId = senderId;
        }

        public Long getReceiverId() {
            return receiverId;
        }

        public void setReceiverId(Long receiverId) {
            this.receiverId = receiverId;
        }

        public String getSenderType() {
            return senderType;
        }

        public void setSenderType(String senderType) {
            this.senderType = senderType;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    /**
     * 聊天消息类，用于传输消息
     */
    public static class ChatMessage {
        private Long senderId;
        private Long receiverId;
        private String senderType;
        private String content;
        private LocalDateTime timestamp;

        // Getters and Setters
        public Long getSenderId() {
            return senderId;
        }

        public void setSenderId(Long senderId) {
            this.senderId = senderId;
        }

        public Long getReceiverId() {
            return receiverId;
        }

        public void setReceiverId(Long receiverId) {
            this.receiverId = receiverId;
        }

        public String getSenderType() {
            return senderType;
        }

        public void setSenderType(String senderType) {
            this.senderType = senderType;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }

    /**
     * 错误DTO类，用于发送错误消息
     */
    public static class ErrorDTO {
        private String message;

        public ErrorDTO(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}