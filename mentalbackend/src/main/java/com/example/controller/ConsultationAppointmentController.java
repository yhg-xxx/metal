package com.example.controller;

import com.example.dto.ConsultationAppointmentDTO;
import com.example.service.ConsultationAppointmentsService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 咨询预约控制器
 */
@RestController
@RequestMapping("/api/appointments")
public class ConsultationAppointmentController {

    @Resource
    private ConsultationAppointmentsService consultationAppointmentsService;

    /**
     * 创建新预约
     */
    @PostMapping
    public ResponseEntity<ConsultationAppointmentDTO> createAppointment(@RequestBody ConsultationAppointmentDTO appointmentDTO) {
        ConsultationAppointmentDTO createdAppointment = consultationAppointmentsService.createAppointment(appointmentDTO);
        return ResponseEntity.ok(createdAppointment);
    }

    /**
     * 获取预约详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConsultationAppointmentDTO> getAppointmentDetail(@PathVariable Long id) {
        ConsultationAppointmentDTO appointment = consultationAppointmentsService.getAppointmentDetail(id);
        if (appointment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(appointment);
    }

    /**
     * 获取用户的预约列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ConsultationAppointmentDTO>> getUserAppointments(@PathVariable Long userId) {
        List<ConsultationAppointmentDTO> appointments = consultationAppointmentsService.getUserAppointments(userId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * 获取咨询师的预约列表
     */
    @GetMapping("/counselor/{counselorId}")
    public ResponseEntity<List<ConsultationAppointmentDTO>> getCounselorAppointments(@PathVariable Long counselorId) {
        List<ConsultationAppointmentDTO> appointments = consultationAppointmentsService.getCounselorAppointments(counselorId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * 更新预约状态
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Boolean> updateAppointmentStatus(@PathVariable Long id, @RequestParam String status) {
        boolean updated = consultationAppointmentsService.updateAppointmentStatus(id, status);
        return updated ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }

    /**
     * 更新支付状态
     */
    @PutMapping("/{id}/payment")
    public ResponseEntity<Boolean> updatePaymentStatus(@PathVariable Long id, @RequestParam String paymentStatus) {
        boolean updated = consultationAppointmentsService.updatePaymentStatus(id, paymentStatus);
        return updated ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }

    /**
     * 取消预约
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Boolean> cancelAppointment(@PathVariable Long id) {
        boolean cancelled = consultationAppointmentsService.cancelAppointment(id);
        return cancelled ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }

    /**
     * 验证时间槽是否可用
     */
    @PostMapping("/validate-time-slot")
    public ResponseEntity<Boolean> validateTimeSlot(@RequestBody ConsultationAppointmentDTO appointmentDTO) {
        boolean available = consultationAppointmentsService.validateTimeSlot(appointmentDTO.getCounselorId(), appointmentDTO);
        return ResponseEntity.ok(available);
    }
}