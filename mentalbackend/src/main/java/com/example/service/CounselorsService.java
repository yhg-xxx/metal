 package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dto.CounselorDTO;
import com.example.entity.Counselors;
import java.util.List;

/**
 * 咨询师服务接口
 */
public interface CounselorsService extends IService<Counselors> {
    // 搜索和筛选咨询师
    List<CounselorDTO> searchAndFilterCounselors(CounselorDTO counselorDTO);
    
    // 获取咨询师详情（包含用户信息和服务设置）
    CounselorDTO getCounselorDetail(Long counselorId);
    
    // 保存或更新咨询师信息
    boolean saveOrUpdateCounselor(CounselorDTO counselorDTO);
    
    // 删除咨询师信息
    boolean deleteCounselor(Long counselorId);
    
    // 获取所有擅长领域（去重）
    List<String> getAllSpecializations();
    
    // 获取所有治疗流派（去重）
    List<String> getAllTherapeuticApproaches();
}