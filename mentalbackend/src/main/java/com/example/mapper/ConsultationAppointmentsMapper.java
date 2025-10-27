package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.ConsultationAppointments;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.time.LocalDateTime;

/**
 * 咨询预约Mapper接口
 */
@Mapper
public interface ConsultationAppointmentsMapper extends BaseMapper<ConsultationAppointments> {
    // 根据用户ID查询预约列表
    List<ConsultationAppointments> findByUserId(@Param("userId") Long userId);
    
    // 根据咨询师ID查询预约列表
    List<ConsultationAppointments> findByCounselorId(@Param("counselorId") Long counselorId);
    
    // 检查时间冲突
    int checkTimeConflict(@Param("counselorId") Long counselorId,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime,
                          @Param("excludeId") Long excludeId);
    
    // 查询用户和咨询师之间的预约
    ConsultationAppointments findByUserAndCounselor(@Param("userId") Long userId,
                                                  @Param("counselorId") Long counselorId,
                                                  @Param("statusList") List<String> statusList);
}