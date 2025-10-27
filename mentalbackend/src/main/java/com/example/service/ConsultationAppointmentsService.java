package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dto.ConsultationAppointmentDTO;
import com.example.entity.ConsultationAppointments;
import java.util.List;

/**
 * 咨询预约服务接口
 */
public interface ConsultationAppointmentsService extends IService<ConsultationAppointments> {
    // 创建新预约
    ConsultationAppointmentDTO createAppointment(ConsultationAppointmentDTO appointmentDTO);
    
    // 获取预约详情
    ConsultationAppointmentDTO getAppointmentDetail(Long appointmentId);
    
    // 获取用户的预约列表
    List<ConsultationAppointmentDTO> getUserAppointments(Long userId);
    
    // 获取咨询师的预约列表
    List<ConsultationAppointmentDTO> getCounselorAppointments(Long counselorId);
    
    // 更新预约状态
    boolean updateAppointmentStatus(Long appointmentId, String status);
    
    // 更新支付状态
    boolean updatePaymentStatus(Long appointmentId, String paymentStatus);
    
    // 取消预约
    boolean cancelAppointment(Long appointmentId);
    
    // 验证预约时间是否可用
    boolean validateTimeSlot(Long counselorId, ConsultationAppointmentDTO appointmentDTO);
    
    // 计算咨询费用
    java.math.BigDecimal calculateFee(Long counselorId, String consultationType, Integer durationMinutes);
}