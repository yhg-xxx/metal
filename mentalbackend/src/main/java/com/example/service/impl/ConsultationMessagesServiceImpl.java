package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.ConsultationMessages;
import com.example.mapper.ConsultationMessagesMapper;
import com.example.service.ConsultationMessagesService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 咨询对话记录服务实现类
 */
@Service
public class ConsultationMessagesServiceImpl extends ServiceImpl<ConsultationMessagesMapper, ConsultationMessages> implements ConsultationMessagesService {

    private static final Logger log = LoggerFactory.getLogger(ConsultationMessagesServiceImpl.class);

    @Resource
    private ConsultationMessagesMapper consultationMessagesMapper;

    @Override
    @Transactional
    public boolean saveMessage(ConsultationMessages message) {
        try {
            // 设置默认值
            if (message.getSentTime() == null) {
                message.setSentTime(LocalDateTime.now());
            }
            if (message.getMessageType() == null) {
                message.setMessageType("TEXT");
            }
            if (message.getReadStatus() == null) {
                message.setReadStatus(false);
            }
            if (message.getConversationType() == null) {
                message.setConversationType("PRE_CONSULTATION");
            }
            return save(message);
        } catch (Exception e) {
            log.error("保存消息失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<ConsultationMessages> getConversationByUserAndCounselor(Long userId, Long counselorId, Integer limit, Integer offset) {
        if (limit == null) {
            limit = 50;
        }
        if (offset == null) {
            offset = 0;
        }
        List<ConsultationMessages> messages = consultationMessagesMapper.getConversationByUserAndCounselor(userId, counselorId, limit, offset);
        return processMessagesTime(messages);
    }

    @Override
    public List<ConsultationMessages> getMessagesByAppointmentId(Long appointmentId) {
        List<ConsultationMessages> messages = consultationMessagesMapper.getMessagesByAppointmentId(appointmentId);
        return processMessagesTime(messages);
    }

    @Override
    @Transactional
    public boolean updateReadStatus(Long id, Boolean readStatus) {
        try {
            return consultationMessagesMapper.updateReadStatus(id, readStatus) > 0;
        } catch (Exception e) {
            log.error("更新消息阅读状态失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean batchUpdateReadStatus(List<Long> ids, Boolean readStatus) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        try {
            return consultationMessagesMapper.batchUpdateReadStatus(ids, readStatus) > 0;
        } catch (Exception e) {
            log.error("批量更新消息阅读状态失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public int getUnreadMessageCount(Long receiverId, String receiverType) {
        QueryWrapper<ConsultationMessages> queryWrapper = new QueryWrapper<>();
        if ("USER".equals(receiverType)) {
            queryWrapper.eq("user_id", receiverId);
            queryWrapper.ne("sender_type", "USER");
        } else if ("COUNSELOR".equals(receiverType)) {
            queryWrapper.eq("counselor_id", receiverId);
            queryWrapper.ne("sender_type", "COUNSELOR");
        }
        queryWrapper.eq("read_status", false);
        // 将long转换为int
        return Math.toIntExact(count(queryWrapper));
    }

    @Override
    public List<ConsultationMessages> getUserConversations(Long userId) {
        QueryWrapper<ConsultationMessages> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("sent_time");
        // 这里可以添加分组逻辑，获取每个咨询师的最新消息
        List<ConsultationMessages> messages = list(queryWrapper);
        return processMessagesTime(messages);
    }

    @Override
    public List<ConsultationMessages> getCounselorConversations(Long counselorId) {
        QueryWrapper<ConsultationMessages> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("counselor_id", counselorId);
        queryWrapper.orderByDesc("sent_time");
        // 这里可以添加分组逻辑，获取每个用户的最新消息
        List<ConsultationMessages> messages = list(queryWrapper);
        return processMessagesTime(messages);
    }
    
    /**
     * 处理消息时间：将北京时间减8小时并格式化为带T的格式
     */
    private List<ConsultationMessages> processMessagesTime(List<ConsultationMessages> messages) {
        if (messages == null || messages.isEmpty()) {
            return messages;
        }
        
        // 由于LocalDateTime是不可变的，我们需要创建新的消息对象来设置格式化后的时间
        // 注意：这里不修改原始的LocalDateTime对象，而是通过自定义序列化或在控制器层处理
        // 但考虑到需求，我们直接修改返回的消息列表中的时间
        for (ConsultationMessages message : messages) {
            if (message.getSentTime() != null) {
                // 将时间减8小时
                LocalDateTime adjustedTime = message.getSentTime().minusHours(8);
                message.setSentTime(adjustedTime);
            }
        }
        return messages;
    }
}