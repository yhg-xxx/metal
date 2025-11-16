package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 咨询对话记录表
 */
@Data
@TableName("consultation_messages")
public class ConsultationMessages implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("appointment_id")
    private Long appointmentId;

    @TableField("sender_type")
    private String senderType; // USER, COUNSELOR

    @TableField("message_type")
    private String messageType; // TEXT, IMAGE, VOICE, SYSTEM

    @TableField("content")
    private String content;

    @TableField("media_url")
    private String mediaUrl;

    @TableField("duration_seconds")
    private Integer durationSeconds;

    @TableField("sent_time")
    private LocalDateTime sentTime;

    @TableField("read_status")
    private Boolean readStatus;

    @TableField("user_id")
    private Long userId;

    @TableField("counselor_id")
    private Long counselorId;

    @TableField("conversation_type")
    private String conversationType; // PRE_CONSULTATION, IN_CONSULTATION, FOLLOW_UP
}