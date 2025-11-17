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
     * OCR 문서 상세 조회
     */
    @PostMapping(value = "/api/getOcrDocumentDetail.do")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getOcrDocumentDetail(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            logger.info("OCR 문서 상세 조회 요청: {}", params);
            
            // 필수 파라미터 검증
            String ctrlYr = (String) params.get("ctrl_yr");
            String instCd = (String) params.get("inst_cd");
            String prdtCd = (String) params.get("prdt_cd");
            String ctrlNo = (String) params.get("ctrl_no");
            
            if (ctrlYr == null || instCd == null || prdtCd == null || ctrlNo == null) {
                throw new IllegalArgumentException("관리번호 정보가 필요합니다.");
            }
            
            // OCR 문서 번호 리스트 조회
            List<String> ocrDocNoList = ocrService.getOcrDocNoList(params);
            
            if (ocrDocNoList == null || ocrDocNoList.isEmpty()) {
                throw new RuntimeException("해당 조건의 문서가 없습니다.");
            }
            
            // 현재 조회할 ocr_doc_no 결정
            String currentOcrDocNo = (String) params.get("ocr_doc_no");
            if (currentOcrDocNo == null || currentOcrDocNo.isEmpty()) {
                // ocr_doc_no가 없으면 첫 번째 문서
                currentOcrDocNo = ocrDocNoList.get(0);
            }
            
            // 현재 인덱스 찾기
            int currentIndex = ocrDocNoList.indexOf(currentOcrDocNo);
            if (currentIndex == -1) {
                currentIndex = 0;
                currentOcrDocNo = ocrDocNoList.get(0);
            }
            
            // 상세 정보 조회
            Map<String, Object> detailParams = new HashMap<>();
            detailParams.put("ocr_doc_no", currentOcrDocNo);
            OcrInfoVO detail = ocrService.getOcrDocumentDetail(detailParams);
            
            // 같은 관리번호의 서류 목록 조회
            List<OcrInfoVO> documentList = ocrService.getDocumentListByCtrlNo(params);
            
            // OCR 결과 텍스트 조회
            Map<String, Object> ocrParams = new HashMap<>();
            ocrParams.put("ocr_doc_no", currentOcrDocNo);
            List<OcrInfoVO> ocrResults = ocrService.getOcrResultText(ocrParams);
            
            logger.info("OCR 결과 조회 - OCR_DOC_NO: {}, 현재 인덱스: {}/{}", 
                currentOcrDocNo, currentIndex + 1, ocrDocNoList.size());
            
            logger.info("OCR 결과 개수: {}", ocrResults != null ? ocrResults.size() : 0);
            if (ocrResults != null && !ocrResults.isEmpty()) {
                OcrInfoVO first = ocrResults.get(0);
                logger.info("첫 번째 OCR 결과 - ITEM_CD: {}, ITEM_NM: {}, ITEM_VALUE: {}", 
                    first.getItem_cd(), first.getItem_nm(), first.getItem_value());
            }
            
            result.put("success", true);
            result.put("data", detail);
            result.put("documentList", documentList);
            result.put("ocrResults", ocrResults);
            result.put("ocrDocNoList", ocrDocNoList);
            result.put("currentIndex", currentIndex);
            result.put("totalPages", ocrDocNoList.size());
            
            logger.info("OCR 문서 상세 조회 완료: {}-{}-{}-{}", ctrlYr, instCd, prdtCd, ctrlNo);
            
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
