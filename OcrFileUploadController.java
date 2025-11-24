package com.refine.ocr.controller;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.refine.common.component.S3Util;
import com.refine.common.service.FileUploadService;

/**
 * OCR 파일 업로드 컨트롤러 (수신부)
 * 파일을 받아서 S3에 업로드
 */
@RestController
public class OcrFileUploadController {
    
    Logger logger = Logger.getLogger(this.getClass().getName());
    
    @Autowired
    private S3Util s3Util;
    
    @Autowired
    private FileUploadService fileUploadService;
    
    /**
     * 파일 업로드 (MultipartFile)
     * 
     * @param file 업로드할 파일
     * @param path S3 저장 경로
     * @return 업로드 결과
     */
    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadImage(
            @org.springframework.web.bind.annotation.RequestParam("file") MultipartFile file,
            @org.springframework.web.bind.annotation.RequestParam("path") String path) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 파라미터 검증
            if (file == null || file.isEmpty()) {
                result.put("success", false);
                result.put("message", "파일이 없습니다.");
                result.put("error_code", "FILE_EMPTY");
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
            }
            
            if (path == null || path.isEmpty()) {
                result.put("success", false);
                result.put("message", "경로가 없습니다.");
                result.put("error_code", "PATH_EMPTY");
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
            }
            
            logger.info("파일 업로드 요청 - PATH: " + path + ", FILE: " + file.getOriginalFilename());
            
            // FileUploadService를 사용하여 S3에 업로드
            fileUploadService.uploadFile(file, path);
            
            result.put("success", true);
            result.put("message", "파일이 성공적으로 업로드되었습니다.");
            result.put("data", new HashMap<String, Object>() {{
                put("path", path);
                put("file_name", file.getOriginalFilename());
                put("file_size", file.getSize());
                put("upload_time", System.currentTimeMillis());
            }});
            
            logger.info("파일 업로드 완료 - PATH: " + path + ", SIZE: " + file.getSize());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);
            
        } catch (Exception e) {
            logger.severe("파일 업로드 실패: " + e.getMessage());
            result.put("success", false);
            result.put("message", "파일 업로드 중 오류가 발생했습니다.");
            result.put("error_code", "UPLOAD_FAILED");
            result.put("error_detail", e.getMessage());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);
        }
    }
}
