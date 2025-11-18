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
import com.refine.common.component.S3Util;

/**
 * OCR 문서 관리 컨트롤러
 */
@Controller
@RequestMapping("/rf_ocr_verf")
public class OcrController {
    
    private static final Logger logger = LoggerFactory.getLogger(OcrController.class);
    
    @Autowired
    private OcrService ocrService;
    
    @Autowired
    private S3Util s3Util;
    
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
     * 이미지 S3에서 조회 (관리번호별 분기)
     */
    @PostMapping(value = "/api/getOcrImage.do")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getOcrImage(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String ocrDocNo = (String) params.get("ocr_doc_no");
            String instCd = (String) params.get("inst_cd");
            String prdtCd = (String) params.get("prdt_cd");
            String imagePath = (String) params.get("image_path");
            
            if (ocrDocNo == null || instCd == null || prdtCd == null || imagePath == null) {
                result.put("success", false);
                result.put("message", "필수 파라미터가 부족합니다.");
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
            }
            
            // 이미지 정보 조회
            Map<String, Object> imageParams = new HashMap<>();
            imageParams.put("ocr_doc_no", ocrDocNo);
            OcrInfoVO imageInfo = ocrService.getImageInfo(imageParams);
            
            if (imageInfo == null) {
                result.put("success", false);
                result.put("message", "이미지 정보를 찾을 수 없습니다.");
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
            }
            
            // 관리번호별 분기 처리
            byte[] imageData = getImageDataByInstitution(instCd, prdtCd, imagePath);
            
            if (imageData == null || imageData.length == 0) {
                result.put("success", false);
                result.put("message", "이미지 데이터를 가져올 수 없습니다.");
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
            }
            
            result.put("success", true);
            result.put("data", imageInfo);
            result.put("imageData", imageData);
            
            logger.info("이미지 조회 완료: OCR_DOC_NO={}, INST_CD={}, PRDT_CD={}", ocrDocNo, instCd, prdtCd);
            
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
            
        } catch (Exception e) {
            logger.error("이미지 조회 실패", e);
            result.put("success", false);
            result.put("message", "이미지 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
        }
    }
    
    /**
     * 관리번호 및 상품별 이미지 데이터 조회
     */
    private byte[] getImageDataByInstitution(String instCd, String prdtCd, String imagePath) {
        try {
            logger.info("이미지 조회 시작: INST_CD={}, PRDT_CD={}, PATH={}", instCd, prdtCd, imagePath);
            
            // 기관별 분기
            switch (instCd) {
                case "01":  // 신한
                    return getImageByShinhan(prdtCd, imagePath);
                    
                case "49":  // 토스
                    return getImageByToss(prdtCd, imagePath);
                    
                case "47":  // 네이버
                    return getImageByNaver(prdtCd, imagePath);
                    
                default:    // 카카오 등 기타
                    return getImageByKakao(prdtCd, imagePath);
            }
        } catch (Exception e) {
            logger.error("이미지 데이터 조회 실패: INST_CD={}, PRDT_CD={}", instCd, prdtCd, e);
            return null;
        }
    }
    
    /**
     * 신한 이미지 조회 (상품별 분기)
     */
    private byte[] getImageByShinhan(String prdtCd, String imagePath) {
        logger.info("신한 이미지 조회: PRDT_CD={}, PATH={}", prdtCd, imagePath);
        
        switch (prdtCd) {
            case "KRH":  // 반환보증
                return s3Util.getObject(imagePath);
            case "KH":   // 저당
                return s3Util.getObject(imagePath);
            case "JL":   // 전세
                return s3Util.getObject(imagePath);
            default:
                return s3Util.getObject(imagePath);
        }
    }
    
    /**
     * 토스 이미지 조회 (상품별 분기)
     */
    private byte[] getImageByToss(String prdtCd, String imagePath) {
        logger.info("토스 이미지 조회: PRDT_CD={}, PATH={}", prdtCd, imagePath);
        
        switch (prdtCd) {
            case "KRH":  // 반환보증
                return s3Util.getObject(imagePath);
            case "KH":   // 저당
                return s3Util.getObject(imagePath);
            case "JL":   // 전세
                return s3Util.getObject(imagePath);
            default:
                return s3Util.getObject(imagePath);
        }
    }
    
    /**
     * 네이버 이미지 조회 (상품별 분기)
     */
    private byte[] getImageByNaver(String prdtCd, String imagePath) {
        logger.info("네이버 이미지 조회: PRDT_CD={}, PATH={}", prdtCd, imagePath);
        
        switch (prdtCd) {
            case "KRH":  // 반환보증
                return s3Util.getObject(imagePath);
            case "KH":   // 저당
                return s3Util.getObject(imagePath);
            case "JL":   // 전세
                return s3Util.getObject(imagePath);
            default:
                return s3Util.getObject(imagePath);
        }
    }
    
    /**
     * 카카오 이미지 조회 (상품별 분기)
     */
    private byte[] getImageByKakao(String prdtCd, String imagePath) {
        logger.info("카카오 이미지 조회: PRDT_CD={}, PATH={}", prdtCd, imagePath);
        
        switch (prdtCd) {
            case "KRH":  // 반환보증
                return s3Util.getObject(imagePath);
            case "KH":   // 저당
                return s3Util.getObject(imagePath);
            case "JL":   // 전세
                return s3Util.getObject(imagePath);
            default:
                return s3Util.getObject(imagePath);
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
