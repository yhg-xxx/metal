package com.example.controller;

import com.example.entity.ConsultationMessages;
import com.example.service.ConsultationMessagesService;
import jakarta.annotation.Resource;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * WebSocket消息控制器 - 优化版
 * 核心功能：
 * 1. 处理私聊消息路由
 * 2. 消息格式转换和验证
 * 3. 错误处理和状态管理
 * 4. WebRTC视频通话信令处理
 */
@Controller
@Slf4j
public class WebSocketController {

    @Resource
    private SimpMessagingTemplate messagingTemplate;
    
    @Resource
    private ConsultationMessagesService consultationMessagesService;
    
    // JSR-380验证器
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * 发送私聊消息（优化版，增加消息验证和错误处理）
     * 消息路由逻辑：
     * 1. 用户发送消息到 /app/chat.private
     * 2. 服务器验证消息格式和内容
     * 3. 服务器根据接收者类型和ID路由消息
     * 4. 同时发送确认消息给发送者
     */
    @MessageMapping("/chat.private")
    public void handlePrivateMessage(@Payload MessageDTO messageDTO) {
        try {
            log.info("收到私聊消息: 发送者={}({}), 接收者={}, 内容={}",
                    messageDTO.getSenderId(), messageDTO.getSenderType(),
                    messageDTO.getReceiverId(), messageDTO.getContent());
            
            // 消息验证
            Set<ConstraintViolation<MessageDTO>> violations = validator.validate(messageDTO);
            if (!violations.isEmpty()) {
                String errorMsg = violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining(", "));
                log.warn("私聊消息验证失败: {}", errorMsg);
                sendErrorMessage(messageDTO.getSenderId(), messageDTO.getSenderType(), "消息格式错误: " + errorMsg);
                return;
            }
            
            // 基本业务逻辑验证
            if (messageDTO.getSenderId() == null || messageDTO.getSenderId() <= 0) {
                sendErrorMessage(messageDTO.getSenderId(), messageDTO.getSenderType(), "发送者ID无效");
                return;
            }
            
            if (messageDTO.getReceiverId() == null || messageDTO.getReceiverId() <= 0) {
                sendErrorMessage(messageDTO.getSenderId(), messageDTO.getSenderType(), "接收者ID无效");
                return;
            }
            
            if (messageDTO.getSenderType() == null || 
                (!"USER".equalsIgnoreCase(messageDTO.getSenderType()) && 
                 !"COUNSELOR".equalsIgnoreCase(messageDTO.getSenderType()))) {
                sendErrorMessage(messageDTO.getSenderId(), messageDTO.getSenderType(), "发送者类型无效");
                return;
            }
            
            if (messageDTO.getContent() == null || messageDTO.getContent().trim().isEmpty()) {
                sendErrorMessage(messageDTO.getSenderId(), messageDTO.getSenderType(), "消息内容不能为空");
                return;
            }

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
            
            // 根据发送者类型设置用户ID和咨询师ID - 忽略大小写比较
            if ("USER".equalsIgnoreCase(messageDTO.getSenderType())) {
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
            // 根据发送者类型确定接收者类型（用户↔咨询师）- 忽略大小写比较
            String receiverType = "USER".equalsIgnoreCase(messageDTO.getSenderType()) ? "COUNSELOR" : "USER";
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
    @Setter
    @Getter
    public static class MessageDTO {
        @NotNull(message = "发送者ID不能为空")
        @Positive(message = "发送者ID必须为正数")
        private Long senderId; // 发送者ID
        
        @NotNull(message = "接收者ID不能为空")
        @Positive(message = "接收者ID必须为正数")
        private Long receiverId; // 接收者ID
        
        @NotNull(message = "发送者类型不能为空")
        @Pattern(regexp = "^(USER|COUNSELOR)$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "发送者类型必须为USER或COUNSELOR")
        private String senderType; // USER, COUNSELOR
        
        @NotNull(message = "消息内容不能为空")
        @NotBlank(message = "消息内容不能为空")
        @Size(max = 1000, message = "消息内容不能超过1000个字符")
        private String content; // 消息内容

    }

    /**
     * 聊天消息类，用于传输消息
     */
    @Setter
    @Getter
    public static class ChatMessage {
        private Long senderId;
        private Long receiverId;
        private String senderType;
        private String content;
        private LocalDateTime timestamp;

    }

    /**
     * 错误DTO类，用于发送错误消息
     */
    @Setter
    @Getter
    public static class ErrorDTO {
        private String message;

        public ErrorDTO(String message) {
            this.message = message;
        }

    }

    /**
     * WebRTC信令消息DTO类，用于处理视频通话的信令交换
     */
    @Setter
    @Getter
    public static class WebRTCSignalDTO {
        @NotNull(message = "发送者ID不能为空")
        @Positive(message = "发送者ID必须为正数")
        private Long senderId;
        
        @NotNull(message = "接收者ID不能为空")
        @Positive(message = "接收者ID必须为正数")
        private Long receiverId;
        
        @NotNull(message = "发送者类型不能为空")
        @Pattern(regexp = "^(USER|COUNSELOR)$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "发送者类型必须为USER或COUNSELOR")
        private String senderType; // USER, COUNSELOR
        
        @NotNull(message = "接收者类型不能为空")
        @Pattern(regexp = "^(USER|COUNSELOR)$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "接收者类型必须为USER或COUNSELOR")
        private String receiverType; // USER, COUNSELOR
        
        @NotNull(message = "信令类型不能为空")
        @Pattern(regexp = "^(offer|answer|ice-candidate)$", message = "信令类型必须为offer、answer或ice-candidate")
        private String type; // offer, answer, ice-candidate
        
        @NotNull(message = "信令数据不能为空")
        @NotBlank(message = "信令数据不能为空")
        private String data; // 信令数据，包含SDP或ICE候选信息
        
        @Size(max = 100, message = "通话ID不能超过100个字符")
        private String callId; // 通话ID，用于标识特定的通话会话

    }

    /**
     * WebRTC通话状态DTO类，用于处理通话状态通知
     */
    @Setter
    @Getter
    public static class WebRTCStatusDTO {
        @NotNull(message = "发送者ID不能为空")
        @Positive(message = "发送者ID必须为正数")
        private Long senderId;
        
        @NotNull(message = "接收者ID不能为空")
        @Positive(message = "接收者ID必须为正数")
        private Long receiverId;
        
        @NotNull(message = "发送者类型不能为空")
        @Pattern(regexp = "^(USER|COUNSELOR)$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "发送者类型必须为USER或COUNSELOR")
        private String senderType; // USER, COUNSELOR
        
        @NotNull(message = "接收者类型不能为空")
        @Pattern(regexp = "^(USER|COUNSELOR)$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "接收者类型必须为USER或COUNSELOR")
        private String receiverType; // USER, COUNSELOR
        
        @NotNull(message = "通话状态不能为空")
        @Pattern(regexp = "^(ringing|accepted|rejected|ended)$", message = "通话状态必须为ringing、accepted、rejected或ended")
        private String status; // ringing, accepted, rejected, ended
        
        @NotNull(message = "通话ID不能为空")
        @Size(max = 100, message = "通话ID不能超过100个字符")
        private String callId;
        
        private LocalDateTime timestamp;

    }

    /**
     * 处理WebRTC信令消息，用于建立点对点视频通话
     * 支持offer、answer、ice-candidate等信令类型
     */
    @MessageMapping("/webrtc.signal")
    public void handleWebRTCSignal(@Payload WebRTCSignalDTO signalDTO) {
        try {
            log.info("收到WebRTC信令消息: 类型={}, 发送者={}({}), 接收者={}, 通话ID={}, 数据长度={}",
                    signalDTO.getType(), signalDTO.getSenderId(), signalDTO.getSenderType(),
                    signalDTO.getReceiverId(), signalDTO.getCallId(),
                    signalDTO.getData() != null ? signalDTO.getData().length() : 0);

            // 消息验证
            if (signalDTO.getSenderId() == null || signalDTO.getSenderId() <= 0) {
                sendErrorMessage(signalDTO.getSenderId(), signalDTO.getSenderType(), "发送者ID无效");
                return;
            }
            
            if (signalDTO.getReceiverId() == null || signalDTO.getReceiverId() <= 0) {
                sendErrorMessage(signalDTO.getSenderId(), signalDTO.getSenderType(), "接收者ID无效");
                return;
            }
            
            if (signalDTO.getSenderType() == null || 
                (!"USER".equalsIgnoreCase(signalDTO.getSenderType()) && 
                 !"COUNSELOR".equalsIgnoreCase(signalDTO.getSenderType()))) {
                sendErrorMessage(signalDTO.getSenderId(), signalDTO.getSenderType(), "发送者类型无效");
                return;
            }
            
            if (signalDTO.getType() == null || signalDTO.getType().isEmpty()) {
                sendErrorMessage(signalDTO.getSenderId(), signalDTO.getSenderType(), "信令类型不能为空");
                return;
            }
            
            if (signalDTO.getData() == null || signalDTO.getData().isEmpty()) {
                sendErrorMessage(signalDTO.getSenderId(), signalDTO.getSenderType(), "信令数据不能为空");
                return;
            }
            
            // 新增：验证接收者类型
            if (signalDTO.getReceiverType() == null || 
                (!"USER".equalsIgnoreCase(signalDTO.getReceiverType()) && 
                 !"COUNSELOR".equalsIgnoreCase(signalDTO.getReceiverType()))) {
                sendErrorMessage(signalDTO.getSenderId(), signalDTO.getSenderType(), "接收者类型无效");
                return;
            }
            
            // 确保callId不为空
            if (signalDTO.getCallId() == null || signalDTO.getCallId().isEmpty()) {
                signalDTO.setCallId("call_" + System.currentTimeMillis());
                log.info("为WebRTC信令生成新的通话ID: {}", signalDTO.getCallId());
            }

            // 根据接收者类型确定路由路径
            String receiverType = signalDTO.getReceiverType().toLowerCase();
            String destination = "/queue/webrtc/" + receiverType + "/" + signalDTO.getReceiverId();

            log.info("WebRTC信令转发到: {}", destination);

            // 转发信令消息给接收者
            messagingTemplate.convertAndSend(destination, signalDTO);
            log.info("WebRTC信令已转发完成");

        } catch (Exception e) {
            log.error("处理WebRTC信令消息异常: {}", e.getMessage(), e);
            // 发送错误消息给发送者
            ErrorDTO errorDTO = new ErrorDTO("WebRTC信令处理失败: " + e.getMessage());
            messagingTemplate.convertAndSend("/queue/errors/" + signalDTO.getSenderType().toLowerCase() + "/" + signalDTO.getSenderId(), errorDTO);
        }
    }

    /**
     * 处理WebRTC通话状态消息
     * 支持ringing、accepted、rejected、ended等状态
     */
    @MessageMapping("/webrtc.status")
    public void handleWebRTCStatus(@Payload WebRTCStatusDTO statusDTO) {
        try {
            log.info("收到WebRTC状态消息: 状态={}, 发送者={}({}), 接收者={}, 通话ID={}",
                    statusDTO.getStatus(), statusDTO.getSenderId(), statusDTO.getSenderType(),
                    statusDTO.getReceiverId(), statusDTO.getCallId());

            // 消息验证
            if (statusDTO.getSenderId() == null || statusDTO.getSenderId() <= 0) {
                sendErrorMessage(statusDTO.getSenderId(), statusDTO.getSenderType(), "发送者ID无效");
                return;
            }
            
            if (statusDTO.getReceiverId() == null || statusDTO.getReceiverId() <= 0) {
                sendErrorMessage(statusDTO.getSenderId(), statusDTO.getSenderType(), "接收者ID无效");
                return;
            }
            
            if (statusDTO.getSenderType() == null || 
                (!"USER".equalsIgnoreCase(statusDTO.getSenderType()) && 
                 !"COUNSELOR".equalsIgnoreCase(statusDTO.getSenderType()))) {
                sendErrorMessage(statusDTO.getSenderId(), statusDTO.getSenderType(), "发送者类型无效");
                return;
            }
            
            if (statusDTO.getStatus() == null || statusDTO.getStatus().isEmpty()) {
                sendErrorMessage(statusDTO.getSenderId(), statusDTO.getSenderType(), "通话状态不能为空");
                return;
            }
            
            // 新增：验证接收者类型
            if (statusDTO.getReceiverType() == null || 
                (!"USER".equalsIgnoreCase(statusDTO.getReceiverType()) && 
                 !"COUNSELOR".equalsIgnoreCase(statusDTO.getReceiverType()))) {
                sendErrorMessage(statusDTO.getSenderId(), statusDTO.getSenderType(), "接收者类型无效");
                return;
            }
            
            // 确保callId不为空
            if (statusDTO.getCallId() == null || statusDTO.getCallId().isEmpty()) {
                statusDTO.setCallId("call_" + System.currentTimeMillis());
                log.info("为WebRTC状态消息生成新的通话ID: {}", statusDTO.getCallId());
            }

            // 设置时间戳
            statusDTO.setTimestamp(LocalDateTime.now());

            // 重要修复：状态消息路由到正确的接收者
            String receiverType = statusDTO.getReceiverType().toLowerCase();
            String destination = "/queue/webrtc/status/" + receiverType + "/" + statusDTO.getReceiverId();

            log.info("WebRTC状态转发到: {}", destination);

            // 转发状态消息给接收者
            messagingTemplate.convertAndSend(destination, statusDTO);
            log.info("WebRTC状态已转发完成");

        } catch (Exception e) {
            log.error("处理WebRTC状态消息异常: {}", e.getMessage(), e);
            // 发送错误消息给发送者
            sendErrorMessage(statusDTO.getSenderId(), statusDTO.getSenderType(), "WebRTC状态处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送错误消息给客户端
     */
    private void sendErrorMessage(Long userId, String userType, String errorMsg) {
        if (userId == null || userType == null) {
            log.warn("无法发送错误消息: 用户ID或类型为空");
            return;
        }
        
        try {
            ErrorDTO errorDTO = new ErrorDTO(errorMsg);
            String destination = "/queue/errors/" + userType.toLowerCase() + "/" + userId;
            messagingTemplate.convertAndSend(destination, errorDTO);
            log.info("已发送错误消息给{}: {}", destination, errorMsg);
        } catch (Exception e) {
            log.error("发送错误消息失败: {}", e.getMessage(), e);
        }
    }
}