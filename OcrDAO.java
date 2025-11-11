package com.refine.ocr.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.refine.ocr.vo.OcrInfoVO;

/**
 * OCR 문서 관리 DAO
 */
@Repository
public interface OcrDAO {
    
    /**
     * OCR 문서 전체 건수 조회
     * 
     * @param params 검색 조건
     * @return 전체 건수
     */
    int getOcrDocumentCount(Map<String, Object> params);
    
    /**
     * OCR 문서 목록 조회
     * 
     * @param params 검색 조건
     * @return OCR 문서 목록
     */
    List<OcrInfoVO> getOcrDocumentList(Map<String, Object> params);
    
    /**
     * OCR 문서 상세 조회
     * 
     * @param ctrlNo 관리번호
     * @return OCR 문서 상세
     */
    OcrInfoVO getOcrDocumentDetail(String ctrlNo);
    
    /**
     * OCR 문서 검증 상태 업데이트
     * 
     * @param params 업데이트 정보
     * @return 업데이트 건수
     */
    int updateVerificationStatus(Map<String, Object> params);
}
