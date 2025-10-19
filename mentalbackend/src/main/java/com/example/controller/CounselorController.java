package com.example.controller;

import com.example.dto.CounselorDTO;
import com.example.service.CounselorsService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 咨询师控制器
 */
@RestController
@RequestMapping("/api/counselors")
public class CounselorController {

    @Resource
    private CounselorsService counselorsService;

    /**
     * 查询咨询师列表（带搜索和筛选功能）
     * 当没有请求体时，返回所有已通过审核的咨询师列表
     */
    @PostMapping("/search")
    public ResponseEntity<List<CounselorDTO>> searchCounselors(@RequestBody(required = false) CounselorDTO counselorDTO) {
        // 如果没有请求体，创建一个新的空DTO对象
        if (counselorDTO == null) {
            counselorDTO = new CounselorDTO();
        }
        List<CounselorDTO> counselors = counselorsService.searchAndFilterCounselors(counselorDTO);
        return ResponseEntity.ok(counselors);
    }

    /**
     * 获取咨询师详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<CounselorDTO> getCounselorDetail(@PathVariable Long id) {
        CounselorDTO counselorDTO = counselorsService.getCounselorDetail(id);
        if (counselorDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(counselorDTO);
    }

    /**
     * 创建咨询师
     */
    @PostMapping
    public ResponseEntity<Boolean> createCounselor(@RequestBody CounselorDTO counselorDTO) {
        boolean result = counselorsService.saveOrUpdateCounselor(counselorDTO);
        return ResponseEntity.ok(result);
    }

    /**
     * 更新咨询师
     */
    @PutMapping
    public ResponseEntity<Boolean> updateCounselor(@RequestBody CounselorDTO counselorDTO) {
        boolean result = counselorsService.saveOrUpdateCounselor(counselorDTO);
        return ResponseEntity.ok(result);
    }

    /**
     * 删除咨询师
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteCounselor(@PathVariable Long id) {
        boolean result = counselorsService.deleteCounselor(id);
        if (!result) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取所有擅长领域（去重）
     */
    @GetMapping("/specializations")
    public ResponseEntity<List<String>> getAllSpecializations() {
        List<String> specializations = counselorsService.getAllSpecializations();
        return ResponseEntity.ok(specializations);
    }
    
    /**
     * 获取所有治疗流派（去重）
     */
    @GetMapping("/approaches")
    public ResponseEntity<List<String>> getAllTherapeuticApproaches() {
        List<String> approaches = counselorsService.getAllTherapeuticApproaches();
        return ResponseEntity.ok(approaches);
    }
}