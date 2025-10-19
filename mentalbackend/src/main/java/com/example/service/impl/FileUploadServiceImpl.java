package com.example.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.URLUtil;
import com.baidu.aip.ocr.AipOcr;
import com.example.service.FileUploadService;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    private static final String UPLOAD_ROOT = System.getProperty("user.dir") + "/uploads/";
    
    // 设置APPID/AK/SK
    private static final String APP_ID = "117338701";
    private static final String API_KEY = "y5HCl51lcEREKxYosKiBTH4V";
    private static final String SECRET_KEY = "5l2rhFBtga9ADs7vb1nGYMnv3knrUKhZ";
    
    // 初始化一个AipOcr单例
    private static final AipOcr client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);

    @Override
    public ResponseEntity<Map<String, Object>> uploadFile(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        if (file.isEmpty()) {
            result.put("code", 400);
            result.put("msg", "文件不能为空");
            return ResponseEntity.badRequest().body(result);
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String safeFileName = FileUtil.getName(originalFilename);
            String storedFilename = Instant.now().toEpochMilli() + "_" + safeFileName;

            Path uploadPath = Paths.get(UPLOAD_ROOT);
            FileUtil.mkdir(uploadPath.toString());

            Path targetPath = uploadPath.resolve(storedFilename);
            FileUtil.writeBytes(file.getBytes(), targetPath.toFile());

            String encodedFileName = URLUtil.encode(storedFilename);
            result.put("code", 200);
            result.put("msg", "success");
            result.put("url", "http://localhost:8080/files/download/" + encodedFileName);
            return ResponseEntity.ok(result);

        } catch (IOException e) {
            result.put("code", 500);
            result.put("msg", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> recognizeText(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        
        if (file.isEmpty()) {
            result.put("code", 400);
            result.put("msg", "文件不能为空");
            return ResponseEntity.badRequest().body(result);
        }
        
        try {
            // 使用空的HashMap作为options参数
            HashMap<String, String> options = new HashMap<>();
            
            // 调用百度OCR接口识别图片中的文字
            byte[] fileBytes = file.getBytes();
            JSONObject res = client.webImage(fileBytes, options);
            
            result.put("code", 200);
            result.put("msg", "识别成功");
            result.put("data", res.toMap());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "识别失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}