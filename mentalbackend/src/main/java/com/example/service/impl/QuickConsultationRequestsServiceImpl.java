package com.example.service.impl;

import java.time.LocalDateTime;
import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.dto.CounselorDTO;
import com.example.entity.ConsultationMessages;
import com.example.entity.Counselors;
import com.example.entity.QuickConsultationRequests;
import com.example.mapper.QuickConsultationRequestsMapper;
import com.example.service.ConsultationMessagesService;
import com.example.service.CounselorsService;
import com.example.service.QuickConsultationRequestsService;

import jakarta.annotation.Resource;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 快速咨询申请服务实现类
 */
@Service
public class QuickConsultationRequestsServiceImpl extends ServiceImpl<QuickConsultationRequestsMapper, QuickConsultationRequests> implements QuickConsultationRequestsService {

    private static final Logger log = LoggerFactory.getLogger(QuickConsultationRequestsServiceImpl.class);

    @Resource
    private QuickConsultationRequestsMapper quickConsultationRequestsMapper;
    
    @Resource
    private CounselorsService counselorsService;
    
    @Resource
    private ConsultationMessagesService consultationMessagesService;
    
    @Override
    public List<CounselorDTO> getMatchedCounselorsByUserId(Long userId) {
        // 查询用户的所有已匹配的快速咨询申请（状态不是PENDING）
        QueryWrapper<QuickConsultationRequests> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .ne("status", "PENDING")
                   .isNotNull("matched_counselor_id");
        
        List<QuickConsultationRequests> requests = list(queryWrapper);
        if (requests == null || requests.isEmpty()) {
            log.info("用户未找到已匹配的咨询申请，用户ID: {}", userId);
            return new ArrayList<>();
        }
        
        // 提取匹配的咨询师ID列表（去重）
        Set<Long> counselorIds = new HashSet<>();
        for (QuickConsultationRequests request : requests) {
            counselorIds.add(request.getMatchedCounselorId());
        }

        // 根据咨询师ID列表查询咨询师详细信息
        List<CounselorDTO> counselorDTOList = new ArrayList<>();
        for (Long counselorId : counselorIds) {
            CounselorDTO counselorDTO = counselorsService.getCounselorDetail(counselorId);
            if (counselorDTO != null) {
                counselorDTOList.add(counselorDTO);
            }
        }
        
        return counselorDTOList;
    }

    @Override
    public boolean addQuickConsultationRequest(QuickConsultationRequests request) {
        // 设置创建时间为当前时间
        request.setCreatedTime(LocalDateTime.now());
        // 设置状态为待处理
        request.setStatus("PENDING");
        // 保存申请记录
        return save(request);
    }
    
    @Transactional
    @Override
    public boolean matchCounselor(Long requestId) {
        // 1. 获取快速咨询申请信息
        QuickConsultationRequests request = getById(requestId);
        if (request == null) {
            log.error("快速咨询申请不存在，申请ID: {}", requestId);
            return false;
        }
        
        // 2. 分析申请信息，提取关键词
        List<String> keywords = extractKeywords(request);
        if (keywords.isEmpty()) {
            log.warn("未能从申请信息中提取到关键词，申请ID: {}", requestId);
            return false;
        }
        
        // 3. 查找匹配的咨询师
        Long matchedCounselorId = findMatchedCounselor(keywords);
        if (matchedCounselorId == null) {
            log.warn("未找到匹配的咨询师，申请ID: {}", requestId);
            return false;
        }
        
        // 4. 更新申请状态和匹配信息
        request.setMatchedCounselorId(matchedCounselorId);
        request.setStatus("MATCHED");
        request.setMatchedTime(LocalDateTime.now());
        
        boolean updated = updateById(request);
        
        // 5. 如果更新成功，发送自动问候消息
        if (updated) {
            try {
                sendGreetingMessage(request.getUserId(), matchedCounselorId);
            } catch (Exception e) {
                log.error("发送问候消息失败，用户ID: {}, 咨询师ID: {}", request.getUserId(), matchedCounselorId, e);
                // 发送消息失败不影响匹配结果
            }
        }
        
        return updated;
    }
    
    /**
     * 发送包含咨询师介绍的问候消息给用户
     */
    private void sendGreetingMessage(Long userId, Long counselorId) {
        // 获取咨询师详情
        CounselorDTO counselorDTO = counselorsService.getCounselorDetail(counselorId);
        if (counselorDTO == null) {
            log.warn("未找到咨询师详情，无法发送问候消息，咨询师ID: {}", counselorId);
            return;
        }
        
        // 构建问候消息内容
        StringBuilder greetingContent = new StringBuilder();
        greetingContent.append("你好！我是")
                      .append(counselorDTO.getRealName() != null ? counselorDTO.getRealName() : "心理咨询师")
                      .append("。");
        
        // 添加咨询师介绍
        if (StringUtils.hasText(counselorDTO.getIntroduction())) {
            greetingContent.append(" ")
                          .append(counselorDTO.getIntroduction());
        } else {
            greetingContent.append(" 很高兴为您提供心理咨询服务，请问有什么可以帮助您的吗？");
        }
        
        // 创建消息对象
        ConsultationMessages message = new ConsultationMessages();
        message.setUserId(userId);
        message.setCounselorId(counselorId);
        message.setSenderType("COUNSELOR"); // 咨询师发送
        message.setMessageType("TEXT"); // 文本消息
        message.setContent(greetingContent.toString());
        message.setSentTime(LocalDateTime.now());
        message.setReadStatus(false); // 初始状态为未读
        message.setConversationType("PRE_CONSULTATION"); // 预咨询阶段
        
        // 保存消息
        boolean saved = consultationMessagesService.saveMessage(message);
        if (saved) {
            log.info("成功发送问候消息给用户，用户ID: {}, 咨询师ID: {}", userId, counselorId);
        } else {
            log.error("保存问候消息失败，用户ID: {}, 咨询师ID: {}", userId, counselorId);
        }
    }
    
    /**
     * 从申请信息中提取关键词
     */
    private List<String> extractKeywords(QuickConsultationRequests request) {
        List<String> keywords = new ArrayList<>();
        
        // 从问题描述中提取关键词
        String problemDescription = request.getProblemDescription();
        if (StringUtils.hasText(problemDescription)) {
            // 根据常见心理问题关键词进行匹配
            addMatchedKeywords(problemDescription, keywords);
        }
        
        // 从图片识别的文字中提取关键词
        String attachedImages = request.getAttachedImages();
        if (StringUtils.hasText(attachedImages)) {
            try {
                JSONArray imagesArray = new JSONArray(attachedImages);
                for (int i = 0; i < imagesArray.length(); i++) {
                    String recognizedText = imagesArray.getJSONObject(i).optString("recognizedText", "");
                    if (StringUtils.hasText(recognizedText)) {
                        addMatchedKeywords(recognizedText, keywords);
                    }
                }
            } catch (Exception e) {
                log.error("解析图片信息失败: {}", e.getMessage());
            }
        }
        
        return keywords;
    }
    
    /**
     * 根据文本内容添加匹配的关键词
     */
    private void addMatchedKeywords(String text, List<String> keywords) {
        // 定义心理问题关键词映射
        Map<String, String[]> keywordMap = new HashMap<>();
        keywordMap.put("焦虑", new String[]{"焦虑情绪", "焦虑", "恐慌", "紧张"});
        keywordMap.put("抑郁", new String[]{"抑郁情绪", "抑郁", "情绪低落", "兴趣减退"});
        keywordMap.put("职场", new String[]{"职场压力", "职业规划", "职业倦怠", "工作压力"});
        keywordMap.put("婚姻", new String[]{"婚姻家庭", "情感问题", "亲密关系"});
        keywordMap.put("亲子", new String[]{"亲子关系", "青少年心理", "家庭教育", "儿童行为问题"});
        keywordMap.put("人际", new String[]{"人际关系", "社交恐惧", "沟通技巧", "边界设定"});
        keywordMap.put("情绪管理", new String[]{"情绪管理", "情绪调节", "情绪控制"});
        keywordMap.put("失眠", new String[]{"睡眠问题", "失眠", "入睡困难"});
        
        // 遍历关键词映射，检查文本中是否包含相关描述
        for (Map.Entry<String, String[]> entry : keywordMap.entrySet()) {
            String category = entry.getKey();
            String[] relatedKeywords = entry.getValue();
            
            for (String keyword : relatedKeywords) {
                if (text.contains(keyword)) {
                    // 添加对应的专业领域标签
                    switch (category) {
                        case "焦虑" -> keywords.add("焦虑情绪");
                        case "抑郁" -> keywords.add("抑郁情绪");
                        case "职场" -> keywords.add("职场压力");
                        case "婚姻" -> keywords.add("婚姻家庭");
                        case "亲子" -> keywords.add("亲子关系");
                        case "人际" -> keywords.add("人际关系");
                        case "情绪管理" -> keywords.add("情绪管理");
                        case "失眠" -> keywords.add("睡眠问题");
                    }
                    break;
                }
            }
        }
    }
    
    /**
     * 根据关键词查找匹配的咨询师
     */
    private Long findMatchedCounselor(List<String> keywords) {
        // 获取所有已通过审核的咨询师
        QueryWrapper<Counselors> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "APPROVED");
        List<Counselors> counselorsList = counselorsService.list(queryWrapper);
        
        // 计算每个咨询师的匹配分数
        Map<Long, Integer> matchScores = new HashMap<>();
        
        for (Counselors counselor : counselorsList) {
            String specialization = counselor.getSpecialization();
            if (StringUtils.hasText(specialization)) {
                try {
                    JSONArray specializationsArray = new JSONArray(specialization);
                    int score = 0;

                    // 检查每个关键词是否在咨询师的擅长领域中
                    for (String keyword : keywords) {
                        for (int i = 0; i < specializationsArray.length(); i++) {
                            if (specializationsArray.getString(i).contains(keyword)) {
                                score++;
                                break;
                            }
                        }
                    }

                    if (score > 0) {
                        matchScores.put(counselor.getId(), score);
                    }
                } catch (Exception e) {
                    log.error("解析咨询师擅长领域失败: {}", e.getMessage());
                }
            }
        }
        
        // 找出分数最高的咨询师
        if (!matchScores.isEmpty()) {
            return matchScores.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
        }
        
        return null;
    }
}