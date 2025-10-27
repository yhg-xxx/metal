package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dto.ConsultationAppointmentDTO;
import com.example.entity.ConsultationAppointments;
import com.example.entity.Counselors;
import com.example.entity.Users;
import com.example.mapper.ConsultationAppointmentsMapper;
import com.example.service.ConsultationAppointmentsService;
import com.example.service.CounselorsService;
import com.example.service.UsersService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 咨询预约服务实现类
 */
@Service
public class ConsultationAppointmentsServiceImpl extends ServiceImpl<ConsultationAppointmentsMapper, ConsultationAppointments> implements ConsultationAppointmentsService {

    private static final Logger log = LoggerFactory.getLogger(ConsultationAppointmentsServiceImpl.class);

    @Resource
    private ConsultationAppointmentsMapper consultationAppointmentsMapper;

    @Resource
    private UsersService usersService;

    @Resource
    private CounselorsService counselorsService;

    @Override
    @Transactional
    public ConsultationAppointmentDTO createAppointment(ConsultationAppointmentDTO appointmentDTO) {
        // 验证参数
        validateAppointmentParams(appointmentDTO);
        
        // 验证咨询师是否存在
        Counselors counselor = counselorsService.getById(appointmentDTO.getCounselorId());
        Assert.notNull(counselor, "咨询师不存在");
        Assert.isTrue("APPROVED".equals(counselor.getStatus()), "咨询师未通过审核或状态异常");
        
        // 验证用户是否存在
        Users user = usersService.getById(appointmentDTO.getUserId());
        Assert.notNull(user, "用户不存在");
        
        // 验证时间槽是否可用
        Assert.isTrue(validateTimeSlot(appointmentDTO.getCounselorId(), appointmentDTO), "该时间段已被预约");
        
        // 计算费用
        BigDecimal fee = calculateFee(appointmentDTO.getCounselorId(), appointmentDTO.getConsultationType(), appointmentDTO.getDurationMinutes());
        appointmentDTO.setFee(fee);
        
        // 创建预约实体
        ConsultationAppointments appointment = new ConsultationAppointments();
        BeanUtils.copyProperties(appointmentDTO, appointment);
        appointment.setStatus("PENDING"); // 默认状态为待确认
        appointment.setPaymentStatus("PENDING"); // 默认支付状态为待支付
        appointment.setCreatedTime(LocalDateTime.now());
        
        // 保存预约
        boolean saved = save(appointment);
        Assert.isTrue(saved, "创建预约失败");
        
        log.info("创建预约成功，预约ID: {}, 用户ID: {}, 咨询师ID: {}", appointment.getId(), appointment.getUserId(), appointment.getCounselorId());
        
        // 返回预约详情
        return getAppointmentDetail(appointment.getId());
    }

    @Override
    public ConsultationAppointmentDTO getAppointmentDetail(Long appointmentId) {
        ConsultationAppointments appointment = getById(appointmentId);
        if (appointment == null) {
            return null;
        }
        
        ConsultationAppointmentDTO dto = new ConsultationAppointmentDTO();
        BeanUtils.copyProperties(appointment, dto);
        
        // 填充用户信息
        Users user = usersService.getById(appointment.getUserId());
        if (user != null) {
            dto.setUserName(user.getUsername());
            dto.setUserPhone(user.getPhone());
        }
        
        // 填充咨询师信息
        Counselors counselor = counselorsService.getById(appointment.getCounselorId());
        if (counselor != null) {
            dto.setCounselorName(counselor.getRealName());
            // 可以从关联的用户表获取手机号
            Users counselorUser = usersService.getById(counselor.getUserId());
            if (counselorUser != null) {
                dto.setCounselorPhone(counselorUser.getPhone());
            }
        }
        
        return dto;
    }

    @Override
    public List<ConsultationAppointmentDTO> getUserAppointments(Long userId) {
        List<ConsultationAppointments> appointments = consultationAppointmentsMapper.findByUserId(userId);
        return convertToDTOList(appointments);
    }

    @Override
    public List<ConsultationAppointmentDTO> getCounselorAppointments(Long counselorId) {
        List<ConsultationAppointments> appointments = consultationAppointmentsMapper.findByCounselorId(counselorId);
        return convertToDTOList(appointments);
    }

    @Override
    public boolean updateAppointmentStatus(Long appointmentId, String status) {
        ConsultationAppointments appointment = getById(appointmentId);
        if (appointment == null) {
            return false;
        }
        appointment.setStatus(status);
        
        // 如果状态变为进行中，记录实际开始时间
        if ("IN_PROGRESS".equals(status)) {
            appointment.setActualStartTime(LocalDateTime.now());
        }
        // 如果状态变为已完成，记录实际结束时间
        else if ("COMPLETED".equals(status)) {
            appointment.setActualEndTime(LocalDateTime.now());
            if (appointment.getActualStartTime() == null) {
                appointment.setActualStartTime(appointment.getScheduledTime());
            }
        }
        
        return updateById(appointment);
    }

    @Override
    public boolean updatePaymentStatus(Long appointmentId, String paymentStatus) {
        ConsultationAppointments appointment = getById(appointmentId);
        if (appointment == null) {
            return false;
        }
        appointment.setPaymentStatus(paymentStatus);
        // 如果支付状态变为已支付，记录支付时间
        if ("PAID".equals(paymentStatus)) {
            appointment.setPaymentTime(LocalDateTime.now());
            // 同时更新预约状态为已确认
            appointment.setStatus("CONFIRMED");
        }
        return updateById(appointment);
    }

    @Override
    public boolean cancelAppointment(Long appointmentId) {
        ConsultationAppointments appointment = getById(appointmentId);
        if (appointment == null) {
            return false;
        }
        
        // 只有待确认和已确认的预约可以取消
        if (!"PENDING".equals(appointment.getStatus()) && !"CONFIRMED".equals(appointment.getStatus())) {
            return false;
        }
        
        appointment.setStatus("CANCELLED");
        // 如果已支付，设置为已退款
        if ("PAID".equals(appointment.getPaymentStatus())) {
            appointment.setPaymentStatus("REFUNDED");
        }
        
        return updateById(appointment);
    }

    @Override
    public boolean validateTimeSlot(Long counselorId, ConsultationAppointmentDTO appointmentDTO) {
        LocalDateTime startTime = appointmentDTO.getScheduledTime();
        LocalDateTime endTime = startTime.plusMinutes(appointmentDTO.getDurationMinutes());
        
        // 检查是否与已有的预约冲突
        int conflictCount = consultationAppointmentsMapper.checkTimeConflict(
                counselorId,
                startTime,
                endTime,
                appointmentDTO.getId() // 更新时排除自身
        );
        
        return conflictCount == 0;
    }

    @Override
    public BigDecimal calculateFee(Long counselorId, String consultationType, Integer durationMinutes) {
        Counselors counselor = counselorsService.getById(counselorId);
        Assert.notNull(counselor, "咨询师不存在");
        
        BigDecimal baseFee = counselor.getConsultationFee();
        Assert.notNull(baseFee, "咨询师未设置咨询费用");
        
        // 根据咨询类型调整费用（例如：视频咨询可能贵一些）
        BigDecimal typeMultiplier = BigDecimal.ONE;
        if ("VIDEO".equals(consultationType)) {
            typeMultiplier = new BigDecimal("1.2");
        } else if ("VOICE".equals(consultationType)) {
            typeMultiplier = new BigDecimal("1.1");
        }
        
        // 根据时长计算费用（按小时计费）
        BigDecimal hours = new BigDecimal(durationMinutes).divide(new BigDecimal(60), 2, BigDecimal.ROUND_HALF_UP);
        
        return baseFee.multiply(typeMultiplier).multiply(hours).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private void validateAppointmentParams(ConsultationAppointmentDTO appointmentDTO) {
        Assert.notNull(appointmentDTO, "预约信息不能为空");
        Assert.notNull(appointmentDTO.getUserId(), "用户ID不能为空");
        Assert.notNull(appointmentDTO.getCounselorId(), "咨询师ID不能为空");
        Assert.notNull(appointmentDTO.getConsultationType(), "咨询类型不能为空");
        Assert.isTrue(Arrays.asList("TEXT", "VOICE", "VIDEO").contains(appointmentDTO.getConsultationType()), "无效的咨询类型");
        Assert.notNull(appointmentDTO.getDurationMinutes(), "咨询时长不能为空");
        Assert.isTrue(appointmentDTO.getDurationMinutes() > 0, "咨询时长必须大于0");
        Assert.notNull(appointmentDTO.getScheduledTime(), "预约时间不能为空");
        Assert.isTrue(appointmentDTO.getScheduledTime().isAfter(LocalDateTime.now()), "预约时间必须在当前时间之后");
    }

    private List<ConsultationAppointmentDTO> convertToDTOList(List<ConsultationAppointments> appointments) {
        List<ConsultationAppointmentDTO> dtoList = new ArrayList<>();
        for (ConsultationAppointments appointment : appointments) {
            ConsultationAppointmentDTO dto = new ConsultationAppointmentDTO();
            BeanUtils.copyProperties(appointment, dto);
            dtoList.add(dto);
        }
        return dtoList;
    }
}