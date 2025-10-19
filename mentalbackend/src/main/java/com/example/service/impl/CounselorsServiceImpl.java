package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dto.CounselorDTO;
import com.example.entity.Counselors;
import com.example.entity.CounselorServiceSettings;
import com.example.entity.Users;
import com.example.mapper.CounselorsMapper;
import com.example.service.CounselorsService;
import com.example.service.CounselorServiceSettingsService;
import com.example.service.UsersService;
import jakarta.annotation.Resource;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.util.ArrayList;
import java.util.List;

/**
 * 咨询师服务实现类
 */
@Service
public class CounselorsServiceImpl extends ServiceImpl<CounselorsMapper, Counselors> implements CounselorsService {

    private static final Logger log = LoggerFactory.getLogger(CounselorsServiceImpl.class);

    @Resource
    private UsersService usersService;

    @Resource
    private CounselorServiceSettingsService counselorServiceSettingsService;

    @Override
    public List<CounselorDTO> searchAndFilterCounselors(CounselorDTO counselorDTO) {
        QueryWrapper<Counselors> queryWrapper = new QueryWrapper<>();
        
        // 关键词搜索
        if (StringUtils.hasText(counselorDTO.getKeyword())) {
            String keyword = "%" + counselorDTO.getKeyword() + "%";
            queryWrapper.and(wrapper -> wrapper
                    .like("real_name", keyword)
                    .or().like("specialization", keyword)
                    .or().like("therapeutic_approach", keyword)
                    .or().like("introduction", keyword)
            );
        }

        // 按状态筛选（默认查询已通过的咨询师）
        queryWrapper.eq("status", "APPROVED");

        // 执行查询
        List<Counselors> counselorsList = baseMapper.selectList(queryWrapper);
        
        // 转换为DTO并关联相关信息
        List<CounselorDTO> result = new ArrayList<>();
        for (Counselors counselor : counselorsList) {
            CounselorDTO dto = convertToDTO(counselor);
            result.add(dto);
        }

        // 进行额外的标签筛选（在内存中进行，因为涉及JSON字段的解析）
        if (!CollectionUtils.isEmpty(counselorDTO.getSpecializationTags()) || 
            !CollectionUtils.isEmpty(counselorDTO.getTherapeuticApproachTags()) || 
            !CollectionUtils.isEmpty(counselorDTO.getServiceTypeTags()) || 
            StringUtils.hasText(counselorDTO.getGenderFilter())) {
            
            List<CounselorDTO> filteredResult = new ArrayList<>();
            for (CounselorDTO dto : result) {
                boolean match = true;
                
                // 擅长领域筛选
                if (!CollectionUtils.isEmpty(counselorDTO.getSpecializationTags()) && StringUtils.hasText(dto.getSpecialization())) {
                    match = counselorDTO.getSpecializationTags().stream().anyMatch(tag -> 
                        dto.getSpecialization().contains(tag)
                    );
                }
                
                // 治疗流派筛选
                if (match && !CollectionUtils.isEmpty(counselorDTO.getTherapeuticApproachTags()) && StringUtils.hasText(dto.getTherapeuticApproach())) {
                    match = counselorDTO.getTherapeuticApproachTags().stream().anyMatch(tag -> 
                        dto.getTherapeuticApproach().contains(tag)
                    );
                }
                
                // 服务类型筛选
                if (match && !CollectionUtils.isEmpty(counselorDTO.getServiceTypeTags()) && StringUtils.hasText(dto.getServiceTypes())) {
                    match = counselorDTO.getServiceTypeTags().stream().anyMatch(tag -> 
                        dto.getServiceTypes().contains(tag)
                    );
                }
                
                // 性别筛选
                if (match && StringUtils.hasText(counselorDTO.getGenderFilter()) && StringUtils.hasText(dto.getGender())) {
                    match = counselorDTO.getGenderFilter().equals(dto.getGender());
                }
                
                if (match) {
                    filteredResult.add(dto);
                }
            }
            result = filteredResult;
        }
        
        return result;
    }

    @Override
    public CounselorDTO getCounselorDetail(Long counselorId) {
        Counselors counselor = baseMapper.selectById(counselorId);
        if (counselor == null) {
            return null;
        }
        return convertToDTO(counselor);
    }

    @Transactional
    @Override
    public boolean saveOrUpdateCounselor(CounselorDTO counselorDTO) {
        // 保存或更新用户信息
        Users user = new Users();
        BeanUtils.copyProperties(counselorDTO, user);
        usersService.saveOrUpdate(user);
        
        // 保存或更新咨询师信息
        Counselors counselor = new Counselors();
        BeanUtils.copyProperties(counselorDTO, counselor);
        counselor.setUserId(user.getId());
        boolean counselorResult = this.saveOrUpdate(counselor);
        
        // 保存或更新咨询师服务设置
        CounselorServiceSettings settings = new CounselorServiceSettings();
        BeanUtils.copyProperties(counselorDTO, settings);
        settings.setCounselorId(counselor.getId());
        boolean settingsResult = counselorServiceSettingsService.saveOrUpdate(settings);
        
        return counselorResult && settingsResult;
    }

    @Transactional
    @Override
    public boolean deleteCounselor(Long counselorId) {
        Counselors counselor = baseMapper.selectById(counselorId);
        if (counselor == null) {
            return false;
        }
        
        // 删除咨询师信息
        boolean counselorResult = this.removeById(counselorId);
        
        // 删除咨询师服务设置
        QueryWrapper<CounselorServiceSettings> settingsQuery = new QueryWrapper<>();
        settingsQuery.eq("counselor_id", counselorId);
        boolean settingsResult = counselorServiceSettingsService.remove(settingsQuery);
        
        // 这里可以选择是否删除关联的用户信息，根据业务需求决定
        // usersService.removeById(counselor.getUserId());
        
        return counselorResult && settingsResult;
    }

    /**
     * 将咨询师实体转换为DTO，并关联用户信息和服务设置
     */
    private CounselorDTO convertToDTO(Counselors counselor) {
        CounselorDTO dto = new CounselorDTO();
        BeanUtils.copyProperties(counselor, dto);
        dto.setCounselorId(counselor.getId());
        
        // 关联用户信息
        Users user = usersService.getById(counselor.getUserId());
        if (user != null) {
            BeanUtils.copyProperties(user, dto);
            dto.setUserId(user.getId());
            dto.setUserStatus(user.getStatus());
        }
        
        // 关联服务设置
        QueryWrapper<CounselorServiceSettings> settingsQuery = new QueryWrapper<>();
        settingsQuery.eq("counselor_id", counselor.getId());
        CounselorServiceSettings settings = counselorServiceSettingsService.getOne(settingsQuery);
        if (settings != null) {
            BeanUtils.copyProperties(settings, dto);
            dto.setServiceSettingsId(settings.getId());
        }
        
        return dto;
    }
    
    @Override
    public List<String> getAllSpecializations() {
        // 创建查询包装器，只查询已通过审核的咨询师
        QueryWrapper<Counselors> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "APPROVED");
        
        // 查询所有已通过审核的咨询师
        List<Counselors> counselorsList = baseMapper.selectList(queryWrapper);
        
        // 使用Set来去重
        Set<String> specializationsSet = new HashSet<>();
        
        // 解析每个咨询师的擅长领域
        for (Counselors counselor : counselorsList) {
            String specializationStr = counselor.getSpecialization();
            if (StringUtils.hasText(specializationStr)) {
                try {
                    // 解析JSON数组字符串
                    JSONArray jsonArray = new JSONArray(specializationStr);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String tag = jsonArray.getString(i);
                        if (StringUtils.hasText(tag)) {
                            specializationsSet.add(tag);
                        }
                    }
                } catch (Exception e) {
                    // 如果解析失败，记录错误但继续处理
                    log.error("解析擅长领域失败: {}", e.getMessage());
                }
            }
        }
        
        // 转换为List并返回
        return new ArrayList<>(specializationsSet);
    }
    
    @Override
    public List<String> getAllTherapeuticApproaches() {
        // 创建查询包装器，只查询已通过审核的咨询师
        QueryWrapper<Counselors> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "APPROVED");
        
        // 查询所有已通过审核的咨询师
        List<Counselors> counselorsList = baseMapper.selectList(queryWrapper);
        
        // 使用Set来去重
        Set<String> approachesSet = new HashSet<>();
        
        // 解析每个咨询师的治疗流派
        for (Counselors counselor : counselorsList) {
            String approachStr = counselor.getTherapeuticApproach();
            if (StringUtils.hasText(approachStr)) {
                try {
                    // 解析JSON数组字符串
                    JSONArray jsonArray = new JSONArray(approachStr);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String tag = jsonArray.getString(i);
                        if (StringUtils.hasText(tag)) {
                            approachesSet.add(tag);
                        }
                    }
                } catch (Exception e) {
                    // 如果解析失败，记录错误但继续处理
                    log.error("解析治疗流派失败: {}", e.getMessage());
                }
            }
        }
        
        // 转换为List并返回
        return new ArrayList<>(approachesSet);
    }
}