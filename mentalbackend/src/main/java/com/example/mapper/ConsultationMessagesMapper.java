package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.ConsultationMessages;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 咨询对话记录Mapper接口
 */
@Mapper
public interface ConsultationMessagesMapper extends BaseMapper<ConsultationMessages> {
    
    // 根据用户ID和咨询师ID获取对话记录
    List<ConsultationMessages> getConversationByUserAndCounselor(
            @Param("userId") Long userId,
            @Param("counselorId") Long counselorId,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );
    
    // 根据预约ID获取对话记录
    List<ConsultationMessages> getMessagesByAppointmentId(@Param("appointmentId") Long appointmentId);
    
    // 更新消息的阅读状态
    int updateReadStatus(@Param("id") Long id, @Param("readStatus") Boolean readStatus);
    
    // 批量更新消息的阅读状态
    int batchUpdateReadStatus(@Param("ids") List<Long> ids, @Param("readStatus") Boolean readStatus);
}