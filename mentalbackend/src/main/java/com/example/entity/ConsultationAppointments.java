package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 咨询预约表
 */
@Data
@TableName("consultation_appointments")
public class ConsultationAppointments implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("counselor_id")
    private Long counselorId;

    @TableField("consultation_type")
    private String consultationType; // TEXT, VOICE, VIDEO

    @TableField("duration_minutes")
    private Integer durationMinutes;

    @TableField("scheduled_time")
    private LocalDateTime scheduledTime;

    @TableField("actual_start_time")
    private LocalDateTime actualStartTime;

    @TableField("actual_end_time")
    private LocalDateTime actualEndTime;

    @TableField("fee")
    private BigDecimal fee;

    @TableField("status")
    private String status; // PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW

    @TableField("payment_status")
    private String paymentStatus; // PENDING, PAID, REFUNDED

    @TableField("payment_time")
    private LocalDateTime paymentTime;

    @TableField("created_time")
    private LocalDateTime createdTime;
}