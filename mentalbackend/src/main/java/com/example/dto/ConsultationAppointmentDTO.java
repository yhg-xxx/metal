package com.example.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 咨询预约DTO
 */
@Data
public class ConsultationAppointmentDTO {
    private Long id;
    private Long userId;
    private Long counselorId;
    private String consultationType; // TEXT, VOICE, VIDEO
    private Integer durationMinutes;
    private LocalDateTime scheduledTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private BigDecimal fee;
    private String status; // PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW
    private String paymentStatus; // PENDING, PAID, REFUNDED
    private LocalDateTime paymentTime;
    private LocalDateTime createdTime;
    
    // 用户信息（扩展）
    private String userName;
    private String userPhone;
    
    // 咨询师信息（扩展）
    private String counselorName;
    private String counselorPhone;
    

}