package com.example.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface FileUploadService {
    
    /**
     * 上传文件并返回文件URL
     * @param file 要上传的文件
     * @return 包含状态码、消息和文件URL的响应
     */
    ResponseEntity<Map<String, Object>> uploadFile(MultipartFile file);
    
    /**
     * 识别图片中的文字
     * @param file 包含文字的图片文件
     * @return 包含状态码、消息和识别结果的响应
     */
    ResponseEntity<Map<String, Object>> recognizeText(MultipartFile file);
}