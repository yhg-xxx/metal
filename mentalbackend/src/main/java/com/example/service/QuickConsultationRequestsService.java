package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dto.CounselorDTO;
import com.example.entity.QuickConsultationRequests;

import java.util.List;

/**
 * 快速咨询申请服务接口
 */
public interface QuickConsultationRequestsService extends IService<QuickConsultationRequests> {
    // 添加快速咨询申请
    boolean addQuickConsultationRequest(QuickConsultationRequests request);
    
    // 根据快速咨询申请智能匹配咨询师
    boolean matchCounselor(Long requestId);
    
    // 根据用户ID查询匹配的咨询师列表
    List<CounselorDTO> getMatchedCounselorsByUserId(Long userId);
}