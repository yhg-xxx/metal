package com.example.controller;

import com.example.dto.CounselorDTO;
import com.example.entity.QuickConsultationRequests;
import com.example.service.QuickConsultationRequestsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 快速咨询申请控制器
 */
@RestController
@RequestMapping("/api/quick-consultation")
public class QuickConsultationController {

    @Resource
    private QuickConsultationRequestsService quickConsultationRequestsService;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private FileUploadController fileUploadController;
    
    /**
     * 根据用户ID查询匹配的咨询师列表
     * 查询快速咨询表中userId=useId且status不是PENDING的matched_counselor_id并去重
     * 返回matched_counselor_id=counselorId的所有CounselorDTO数据
     */
    @GetMapping("/matched-counselors")
    public ResponseEntity<Map<String, Object>> getMatchedCounselorsByUserId(@RequestParam("useId") Long useId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (useId == null) {
                result.put("code", 400);
                result.put("msg", "用户ID不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            
            List<CounselorDTO> counselorDTOList = quickConsultationRequestsService.getMatchedCounselorsByUserId(useId);
            
            result.put("code", 200);
            result.put("msg", "查询成功");
            result.put("data", counselorDTOList);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 添加快速咨询申请
     * 必填字段：userId, problemDescription, problemDuration, preferredMethod
     * 状态默认为PENDING，创建时间为当前时间
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addQuickConsultationRequest(
            @RequestParam("userId") Long userId,
            @RequestParam("problemDescription") String problemDescription,
            @RequestParam("problemDuration") String problemDuration,
            @RequestParam("preferredMethod") String preferredMethod,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 验证必填字段
            if (userId == null) {
                result.put("code", 400);
                result.put("msg", "用户ID不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            if (problemDescription == null || problemDescription.isEmpty()) {
                result.put("code", 400);
                result.put("msg", "问题描述不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            if (problemDuration == null || problemDuration.isEmpty()) {
                result.put("code", 400);
                result.put("msg", "问题持续时间不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            if (preferredMethod == null || preferredMethod.isEmpty()) {
                result.put("code", 400);
                result.put("msg", "偏好咨询方式不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            // 创建快速咨询申请对象
            QuickConsultationRequests request = new QuickConsultationRequests();
            request.setUserId(userId);
            request.setProblemDescription(problemDescription);
            request.setProblemDuration(problemDuration);
            request.setPreferredMethod(preferredMethod);

            // 处理图片文件
            if (files != null && files.length > 0) {
                JSONArray imagesArray = new JSONArray();
                
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        // 1. 上传图片文件
                        ResponseEntity<Map<String, Object>> uploadResult = fileUploadController.uploadFile(file);
                        if (uploadResult.getStatusCode().is2xxSuccessful() && 
                            uploadResult.getBody() != null && 
                            uploadResult.getBody().containsKey("url")) {
                            
                            String imageUrl = (String) uploadResult.getBody().get("url");
                            
                            // 2. 识别图片中的文字
                            String recognizedText = "";
                            try {
                                ResponseEntity<Map<String, Object>> ocrResult = fileUploadController.recognizeText(file);
                                if (ocrResult.getStatusCode().is2xxSuccessful() && 
                                    ocrResult.getBody() != null && 
                                    ocrResult.getBody().containsKey("data")) {
                                    
                                    Map<String, Object> ocrData = (Map<String, Object>) ocrResult.getBody().get("data");
                                    if (ocrData.containsKey("words_result")) {
                                        List<Map<String, String>> wordsResult = (List<Map<String, String>>) ocrData.get("words_result");
                                        StringBuilder textBuilder = new StringBuilder();
                                        for (Map<String, String> word : wordsResult) {
                                            if (word.containsKey("words")) {
                                                textBuilder.append(word.get("words")).append("\n");
                                            }
                                        }
                                        recognizedText = textBuilder.toString();
                                    }
                                }
                            } catch (Exception e) {
                                // 文字识别失败不影响主流程
                                recognizedText = "文字识别失败: " + e.getMessage();
                            }
                            
                            // 3. 创建包含图片URL和识别文字的JSON对象
                            JSONObject imageObj = new JSONObject();
                            imageObj.put("url", imageUrl);
                            imageObj.put("recognizedText", recognizedText);
                            imagesArray.put(imageObj);
                        }
                    }
                }
                
                // 将图片信息存入attachedImages字段
                if (imagesArray.length() > 0) {
                    request.setAttachedImages(imagesArray.toString());
                }
            }

            // 调用服务层添加申请
            boolean saved = quickConsultationRequestsService.addQuickConsultationRequest(request);
            if (saved) {
                // 尝试智能匹配咨询师
                boolean matched = quickConsultationRequestsService.matchCounselor(request.getId());
                
                // 重新获取更新后的申请信息
                QuickConsultationRequests updatedRequest = quickConsultationRequestsService.getById(request.getId());
                
                result.put("code", 200);
                result.put("msg", matched ? "申请成功并匹配到咨询师" : "申请成功，正在为您匹配咨询师");
                result.put("data", updatedRequest);
                return ResponseEntity.ok(result);
            } else {
                result.put("code", 500);
                result.put("msg", "快速咨询申请添加失败");
                return ResponseEntity.status(500).body(result);
            }

        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "快速咨询申请添加失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
}