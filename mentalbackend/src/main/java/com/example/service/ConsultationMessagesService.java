package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.ConsultationMessages;
import java.util.List;

/**
 * 咨询对话记录服务接口
 */
public interface ConsultationMessagesService extends IService<ConsultationMessages> {
    
    // 保存消息
    boolean saveMessage(ConsultationMessages message);
    
    // 根据用户ID和咨询师ID获取对话记录
    List<ConsultationMessages> getConversationByUserAndCounselor(Long userId, Long counselorId, Integer limit, Integer offset);
    
    // 根据预约ID获取对话记录
    List<ConsultationMessages> getMessagesByAppointmentId(Long appointmentId);
    
    // 更新消息的阅读状态
    boolean updateReadStatus(Long id, Boolean readStatus);
    
    // 批量更新消息的阅读状态
    boolean batchUpdateReadStatus(List<Long> ids, Boolean readStatus);
    
    // 获取未读消息数量
    int getUnreadMessageCount(Long receiverId, String receiverType);
    
    // 获取用户与咨询师之间的会话列表
    List<ConsultationMessages> getUserConversations(Long userId);
    
    // 获取咨询师与用户之间的会话列表
    List<ConsultationMessages> getCounselorConversations(Long counselorId);
}