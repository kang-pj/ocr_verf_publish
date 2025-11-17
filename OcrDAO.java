package com.refine.ocr.dao;

import java.util.List;
import java.util.Map;

import com.refine.ocr.vo.OcrInfoVO;

/**
 * OCR 문서 관리 DAO 인터페이스
 */
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
     * @param params 관리번호 정보
     * @return OCR 문서 상세
     */
    OcrInfoVO getOcrDocumentDetail(Map<String, Object> params);
    
    /**
     * 같은 관리번호의 서류 목록 조회
     * 
     * @param params 관리번호 정보
     * @return 서류 목록
     */
    List<OcrInfoVO> getDocumentListByCtrlNo(Map<String, Object> params);
    
    /**
     * OCR 결과 텍스트 조회
     * 
     * @param params 관리번호 정보
     * @return OCR 결과 목록
     */
    List<OcrInfoVO> getOcrResultText(Map<String, Object> params);
    
    /**
     * 특정 서류 타입의 OCR 문서 번호 리스트 조회
     * 
     * @param params 관리번호 + 서류타입 정보
     * @return OCR 문서 번호 리스트
     */
    List<String> getOcrDocNoList(Map<String, Object> params);
    
    /**
     * OCR 문서 검증 상태 업데이트
     * 
     * @param params 업데이트 정보
     * @return 업데이트 건수
     */
    int updateVerificationStatus(Map<String, Object> params);
}
