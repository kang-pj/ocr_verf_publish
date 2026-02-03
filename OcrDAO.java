package com.refine.ocr.dao;

import java.util.List;
import java.util.Map;

import com.refine.ocr.vo.OcrInfoVO;

/**
 * OCR 문서 관리 DAO 인터페이스
 */
public interface OcrDAO {
    
    int getOcrDocumentCount(Map<String, Object> params);
    
    List<OcrInfoVO> getOcrDocumentList(Map<String, Object> params);
    
    OcrInfoVO getOcrDocumentDetail(Map<String, Object> params);
    
    List<OcrInfoVO> getDocumentListByCtrlNo(Map<String, Object> params);
    
    List<OcrInfoVO> getAllFilesByCtrlNo(Map<String, Object> params);
    
    List<OcrInfoVO> getOcrResultText(Map<String, Object> params);
    
    List<String> getOcrDocNoList(Map<String, Object> params);
    
    int updateVerificationStatus(Map<String, Object> params);
    
    int updateOcrStatus(Map<String, Object> params);
    
    String getDocumentName(Map<String, Object> params);
    
    List<Map<String, Object>> getDocumentNamesBatch(Map<String, Object> params);
    
    OcrInfoVO getImageInfo(Map<String, Object> params);
    
    String getSysClsCd(Map<String, Object> params);
    
    String getMaxCtrlNo(Map<String, Object> params);
    
    int insertOcrDocument(Map<String, Object> params);
    
    int deleteOcrResult(Map<String, Object> params);
    
    int deleteOcrDocument(Map<String, Object> params);
    
    List<String> getOcrDocNoListByUserId(Map<String, Object> params);
    
    List<String> getUserTestFilePathList(Map<String, Object> params);
    
    int deleteUserTestDocuments(Map<String, Object> params);
    
    // OCR 항목 코드 관리
    List<com.refine.ocr.vo.OcrItemVO> getOcrItemList(Map<String, Object> params);
    
    int getOcrItemCount(Map<String, Object> params);
    
    int checkOcrItemExists(Map<String, Object> params);
    
    int insertOcrItem(Map<String, Object> params);
    
    int updateOcrItem(Map<String, Object> params);
    
    int deleteOcrItem(Map<String, Object> params);
    
    int activateOcrItem(Map<String, Object> params);
    
    // 문서 유형 목록 조회
    List<Map<String, Object>> getDocumentTypes(Map<String, Object> params);
    
    // OCR 결과 번호로 조회
    OcrInfoVO getOcrByRsltNo(Map<String, Object> params);
    
    // OCR 추출 데이터 배치 저장
    int insertOcrExtractDataBatch(Map<String, Object> params);
    
    // OCR 추출 데이터 존재 여부 확인
    int checkOcrExtractDataExists(Map<String, Object> params);
    
    // OCR 추출 데이터 조회
    List<Map<String, Object>> getOcrExtractData(Map<String, Object> params);
    
    // OCR 추출 데이터 fail_type 업데이트
    int updateOcrExtractFailType(Map<String, Object> params);
    
    // OCR 추출 데이터 fail_type INSERT
    int insertOcrExtractFailType(Map<String, Object> params);
    
    // OCR 체크 완료 상태 추가
    int insertCheckCompleted(Map<String, Object> params);
    
    // OCR 체크 완료 상태 업데이트
    int updateCheckCompleted(Map<String, Object> params);
    
    // OCR 체크 완료 상태 삭제
    int deleteCheckCompleted(Map<String, Object> params);
    
    // OCR 추출 데이터 삭제
    int deleteOcrExtractData(Map<String, Object> params);
}
