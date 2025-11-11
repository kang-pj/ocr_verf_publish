package com.refine.ocr.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.refine.ocr.service.OcrService;
import com.refine.ocr.vo.OcrInfoVO;

/**
 * OCR 문서 관리 컨트롤러
 */
@Controller
@RequestMapping("/rf_ocr_verf")
public class OcrController {
    
    private static final Logger logger = LoggerFactory.getLogger(OcrController.class);
    
    @Autowired
    private OcrService ocrService;
    
    /**
     * OCR 결과 목록 조회 (AJAX)
     * 
     * @param params 검색 조건
     * @return JSON 응답
     */
    @PostMapping(value = "/api/getOcrResultList.do")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getOcrResultList(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            logger.info("OCR 결과 목록 조회 요청: {}", params);
            
            // 전체 건수 조회
            int totalCount = ocrService.getOcrDocumentCount(params);
            
            // 데이터 조회 (Service에서 검증 및 기본값 설정)
            List<OcrInfoVO> list = ocrService.getOcrDocumentList(params);
            
            result.put("success", true);
            result.put("data", list);
            result.put("recordsTotal", totalCount);
            result.put("recordsFiltered", totalCount);
            
            logger.info("OCR 결과 목록 조회 완료: {} 건 (전체: {} 건)", list.size(), totalCount);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);
            
        } catch (Exception e) {
            logger.error("OCR 결과 목록 조회 실패", e);
            result.put("success", false);
            result.put("message", "데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);
        }
    }
    
    /**
     * OCR 문서 상세 조회 (예시)
     */
    @PostMapping(value = "/api/getOcrDocumentDetail.do")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getOcrDocumentDetail(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String ctrlNo = (String) params.get("ctrl_no");
            
            if (ctrlNo == null || ctrlNo.trim().isEmpty()) {
                throw new IllegalArgumentException("관리번호가 필요합니다.");
            }
            
            OcrInfoVO detail = ocrService.getOcrDocumentDetail(ctrlNo);
            
            result.put("success", true);
            result.put("data", detail);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);
            
        } catch (Exception e) {
            logger.error("OCR 문서 상세 조회 실패", e);
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);
        }
    }
}
