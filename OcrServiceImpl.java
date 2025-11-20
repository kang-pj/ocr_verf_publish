package com.refine.ocr.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.refine.ocr.dao.OcrDAO;
import com.refine.ocr.service.OcrService;
import com.refine.ocr.vo.OcrInfoVO;
import com.refine.common.component.S3Util;
import com.refine.common.component.Aes256Util;

/**
 * OCR 문서 관리 서비스 구현체
 */
@Service
@Transactional(readOnly = true)
public class OcrServiceImpl implements OcrService {
    
    private static final Logger logger = LoggerFactory.getLogger(OcrServiceImpl.class);
    
    @Autowired
    private OcrDAO ocrDAO;
    
    @Autowired
    private S3Util s3Util;
    
    @Autowired
    private Aes256Util aes256Util;
    
    @Override
    public int getOcrDocumentCount(Map<String, Object> params) {
        try {
            return ocrDAO.getOcrDocumentCount(params);
        } catch (Exception e) {
            logger.error("OCR 문서 건수 조회 중 오류 발생", e);
            return 0;
        }
    }
    
    @Override
    public List<OcrInfoVO> getOcrDocumentList(Map<String, Object> params) {
        logger.debug("OCR 문서 목록 조회 시작: {}", params);
        
        // 파라미터 검증 및 기본값 설정
        validateAndSetDefaults(params);
        
        try {
            List<OcrInfoVO> list = ocrDAO.getOcrDocumentList(params);
            
            logger.debug("OCR 문서 목록 조회 완료: {} 건", list != null ? list.size() : 0);
            
            return list;
            
        } catch (Exception e) {
            logger.error("OCR 문서 목록 조회 중 오류 발생", e);
            throw new RuntimeException("데이터베이스 조회 중 오류가 발생했습니다.", e);
        }
    }
    
    @Override
    public OcrInfoVO getOcrDocumentDetail(Map<String, Object> params) {
        logger.debug("OCR 문서 상세 조회: {}", params);
        
        try {
            OcrInfoVO detail = ocrDAO.getOcrDocumentDetail(params);
            
            if (detail == null) {
                throw new RuntimeException("해당 문서를 찾을 수 없습니다.");
            }
            
            return detail;
            
        } catch (Exception e) {
            logger.error("OCR 문서 상세 조회 중 오류 발생", e);
            throw new RuntimeException("데이터베이스 조회 중 오류가 발생했습니다.", e);
        }
    }
    
    @Override
    public List<OcrInfoVO> getDocumentListByCtrlNo(Map<String, Object> params) {
        logger.debug("서류 목록 조회: {}", params);
        
        try {
            return ocrDAO.getDocumentListByCtrlNo(params);
        } catch (Exception e) {
            logger.error("서류 목록 조회 중 오류 발생", e);
            throw new RuntimeException("데이터베이스 조회 중 오류가 발생했습니다.", e);
        }
    }
    
    @Override
    public List<OcrInfoVO> getOcrResultText(Map<String, Object> params) {
        logger.debug("OCR 결과 텍스트 조회: {}", params);
        
        try {
            return ocrDAO.getOcrResultText(params);
        } catch (Exception e) {
            logger.error("OCR 결과 텍스트 조회 중 오류 발생", e);
            throw new RuntimeException("데이터베이스 조회 중 오류가 발생했습니다.", e);
        }
    }
    
    @Override
    public List<String> getOcrDocNoList(Map<String, Object> params) {
        logger.debug("OCR 문서 번호 리스트 조회: {}", params);
        
        try {
            return ocrDAO.getOcrDocNoList(params);
        } catch (Exception e) {
            logger.error("OCR 문서 번호 리스트 조회 중 오류 발생", e);
            throw new RuntimeException("데이터베이스 조회 중 오류가 발생했습니다.", e);
        }
    }
    
    @Override
    @Transactional(readOnly = false)
    public int updateVerificationStatus(Map<String, Object> params) {
        logger.info("OCR 문서 검증 상태 업데이트: {}", params);
        
        try {
            int result = ocrDAO.updateVerificationStatus(params);
            
            logger.info("OCR 문서 검증 상태 업데이트 완료: {} 건", result);
            
            return result;
            
        } catch (Exception e) {
            logger.error("OCR 문서 검증 상태 업데이트 중 오류 발생", e);
            throw new RuntimeException("데이터베이스 업데이트 중 오류가 발생했습니다.", e);
        }
    }
    
    @Override
    public OcrInfoVO getImageInfo(Map<String, Object> params) {
        logger.debug("이미지 정보 조회: {}", params);
        
        try {
            OcrInfoVO imageInfo = ocrDAO.getImageInfo(params);
            
            if (imageInfo == null) {
                throw new RuntimeException("이미지 정보를 찾을 수 없습니다.");
            }
            
            return imageInfo;
            
        } catch (Exception e) {
            logger.error("이미지 정보 조회 중 오류 발생", e);
            throw new RuntimeException("이미지 정보 조회 중 오류가 발생했습니다.", e);
        }
    }
    
    @Override
    public byte[] getOcrImage(String instCd, String prdtCd, String imagePath) {
        logger.debug("OCR 이미지 조회: INST_CD={}, PRDT_CD={}, PATH={}", instCd, prdtCd, imagePath);
        
        try {
            // imagePath Base64 디코딩
            byte[] decodedBytes = Base64.getDecoder().decode(imagePath);
            imagePath = new String(decodedBytes, StandardCharsets.UTF_8);
            
            // 상품 분류 조회
            String sysClsCd = getSysClsCd(instCd, prdtCd);
            logger.info("상품 분류: SYS_CLS_CD={}", sysClsCd);
            
            // 상품 분류별 처리
            if ("KRH".equals(sysClsCd)) {
                return getImageByKRH(instCd, prdtCd, imagePath);
            } else if ("JL".equals(sysClsCd) || "KH".equals(sysClsCd) || "ITL".equals(sysClsCd) || 
                       "M".equals(sysClsCd) || "IM".equals(sysClsCd) || "SCLS".equals(sysClsCd) || "BY".equals(sysClsCd)) {
                return getImageByLease(imagePath);
            } else {
                return getImageFromS3(imagePath);
            }
        } catch (Exception e) {
            logger.error("이미지 조회 실패: INST_CD={}, PRDT_CD={}", instCd, prdtCd, e);
            throw new RuntimeException("이미지 조회 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 반환보증(KRH) 이미지 조회 - 기관별 처리
     */
    private byte[] getImageByKRH(String instCd, String prdtCd, String imagePath) {
        logger.info("반환보증 이미지 조회: INST_CD={}", instCd);
        
        if ("49".equals(instCd)) {  // 토스
            return getImageByToss(imagePath);
        } else if ("01".equals(instCd)) {  // 신한
            return getImageByShinhan(imagePath);
        } else if ("47".equals(instCd)) {  // 네이버
            return getImageByNaver(imagePath);
        } else if ("45".equals(instCd)) {  // 카카오
            return getImageByKakao(imagePath);
        } else {
            logger.warn("지원하지 않는 기관: INST_CD={}", instCd);
            return null;
        }
    }
    
    /**
     * 토스 이미지 조회
     */
    private byte[] getImageByToss(String imagePath) {
        logger.info("토스 이미지 조회");
        try {
            return s3Util.getObject("49", imagePath);
        } catch (Exception e) {
            logger.error("토스 이미지 조회 실패", e);
            throw new RuntimeException("토스 이미지 조회 실패", e);
        }
    }
    
    /**
     * 신한 이미지 조회
     */
    private byte[] getImageByShinhan(String imagePath) {
        logger.info("신한 이미지 조회");
        try {
            return s3Util.getObject("01", imagePath);
        } catch (Exception e) {
            logger.error("신한 이미지 조회 실패", e);
            throw new RuntimeException("신한 이미지 조회 실패", e);
        }
    }
    
    /**
     * 네이버 이미지 조회
     */
    private byte[] getImageByNaver(String imagePath) {
        logger.info("네이버 이미지 조회");
        try {
            return s3Util.getObject("47", imagePath);
        } catch (Exception e) {
            logger.error("네이버 이미지 조회 실패", e);
            throw new RuntimeException("네이버 이미지 조회 실패", e);
        }
    }
    
    /**
     * 카카오 이미지 조회
     */
    private byte[] getImageByKakao(String imagePath) {
        logger.info("카카오 이미지 조회");
        try {
            return s3Util.getObject("45", imagePath);
        } catch (Exception e) {
            logger.error("카카오 이미지 조회 실패", e);
            throw new RuntimeException("카카오 이미지 조회 실패", e);
        }
    }
    
    /**
     * 전세/저당 등 임차인 이미지 조회
     */
    private byte[] getImageByLease(String imagePath) {
        logger.info("임차인 이미지 조회");
        try {
            return s3Util.getObject("default", imagePath);
        } catch (Exception e) {
            logger.error("임차인 이미지 조회 실패", e);
            throw new RuntimeException("임차인 이미지 조회 실패", e);
        }
    }
    
    @Override
    public String getSysClsCd(String instCd, String prdtCd) {
        logger.debug("상품 분류 조회: INST_CD={}, PRDT_CD={}", instCd, prdtCd);
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("instCd", instCd);
            params.put("prdtCd", prdtCd);
            String sysClsCd = ocrDAO.getSysClsCd(params);
            
            if (sysClsCd == null) {
                logger.warn("상품 분류를 찾을 수 없음: INST_CD={}, PRDT_CD={}", instCd, prdtCd);
                return prdtCd;
            }
            
            return sysClsCd;
        } catch (Exception e) {
            logger.warn("상품 분류 조회 실패: INST_CD={}, PRDT_CD={}", instCd, prdtCd, e);
            return prdtCd;
        }
    }
    
    /**
     * 상품 분류 조회 (private)
     */
    private String getSysClsCodePrivate(String instCd, String prdtCd) {
        return getSysClsCd(instCd, prdtCd);
    }
    
    /**
     * 기본 S3 이미지 조회
     */
    private byte[] getImageFromS3(String imagePath) {
        logger.info("S3에서 이미지 조회: {}", imagePath);
        try {
            return s3Util.getObject("default", imagePath);
        } catch (Exception e) {
            logger.error("S3 이미지 조회 실패", e);
            throw new RuntimeException("S3 이미지 조회 실패", e);
        }
    }
    
    /**
     * 파라미터 검증 및 기본값 설정
     */
    private void validateAndSetDefaults(Map<String, Object> params) {
        // 페이징 기본값
        if (params.get("paging") == null) {
            params.put("paging", 0);
        }
        if (params.get("num") == null) {
            params.put("num", 50);
        }
        
        // 정렬 기본값
        if (params.get("sort") == null) {
            params.put("sort", "DESC");
        }
        
        // 빈 문자열을 null로 변환
        params.forEach((key, value) -> {
            if (value instanceof String && ((String) value).trim().isEmpty()) {
                params.put(key, null);
            }
        });
        
        // 날짜 형식 검증
        String startDate = (String) params.get("ins_dttm_st");
        String endDate = (String) params.get("ins_dttm_en");
        
        if (startDate != null && !startDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException("시작일 형식이 올바르지 않습니다: " + startDate);
        }
        if (endDate != null && !endDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException("종료일 형식이 올바르지 않습니다: " + endDate);
        }
    }
}
