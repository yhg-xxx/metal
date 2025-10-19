package com.example.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ContentDisposition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cn.hutool.core.io.FileUtil;
import com.example.service.FileUploadService;

@RestController
@RequestMapping("/files")
public class FileUploadController {

    private static final String UPLOAD_ROOT = System.getProperty("user.dir") + "/uploads/";
    
    @Resource
    private FileUploadService fileUploadService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file) {
        // 调用服务层进行文件上传
        return fileUploadService.uploadFile(file);
    }

    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) throws IOException {
        String filePath = UPLOAD_ROOT;
        String realPath = filePath + fileName;

        if (!FileUtil.exist(realPath)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
            return;
        }

        try {
            ContentDisposition contentDisposition = ContentDisposition.attachment()
                    .filename(fileName, StandardCharsets.UTF_8)
                    .build();
            response.setHeader("Content-Disposition", contentDisposition.toString());

            try (OutputStream os = response.getOutputStream()) {
                os.write(FileUtil.readBytes(realPath));
                os.flush();
            }
        } catch (IOException e) {
            throw new IOException("文件下载失败", e);
        }
    }
/**
        * wang-editor编辑器文件上传接口
 */
    @PostMapping("/wang/upload")
    public Map<String, Object> wangEditorUpload(MultipartFile file) {
        String flag = System.currentTimeMillis() + "";
        String fileName = file.getOriginalFilename();

        try {
            String filePath = System.getProperty("user.dir") + "/uploads/";
            // 文件存储形式：时间戳-文件名
            FileUtil.writeBytes(file.getBytes(), filePath + flag + "-" + fileName);
            System.out.println(fileName + "--上传成功");
            Thread.sleep(1L);
        } catch (Exception e) {
            System.err.println(fileName + "--文件上传失败");
        }

        String http = "http://localhost:8080/files/download/";
        Map<String, Object> resMap = new HashMap<>();

        // 构建返回参数（需根据实际工具类调整）
        resMap.put("errno", 0);
        resMap.put("data", Collections.singletonList(
                new HashMap<String, String>() {{
                    put("url", http + flag + "-" + fileName);
                }}
        ));

        return resMap;
    }
    
    /**
     * 图片文字识别接口
     * @param file 上传的图片文件
     * @return 识别结果
     */
    @PostMapping("/ocr")
    public ResponseEntity<Map<String, Object>> recognizeText(@RequestParam("file") MultipartFile file) {
        // 调用服务层进行文字识别
        return fileUploadService.recognizeText(file);
    }
}