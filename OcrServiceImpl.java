package com.refine.ocr.service.impl;

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

/**
 * OCR 문서 관리 서비스 구현체
 */
@Service
@Transactional(readOnly = true)
public class OcrServiceImpl implements OcrService {
    
    private static final Logger logger = LoggerFactory.getLogger(OcrServiceImpl.class);
    
    @Autowired
    private OcrDAO ocrDAO;
    
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
