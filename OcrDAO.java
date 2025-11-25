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
    
    List<OcrInfoVO> getOcrResultText(Map<String, Object> params);
    
    List<String> getOcrDocNoList(Map<String, Object> params);
    
    int updateVerificationStatus(Map<String, Object> params);
    
    OcrInfoVO getImageInfo(Map<String, Object> params);
    
    String getSysClsCd(Map<String, Object> params);
    
    String getMaxCtrlNo(Map<String, Object> params);
    
    int insertOcrDocument(Map<String, Object> params);
    
    int deleteOcrResult(Map<String, Object> params);
    
    int deleteOcrDocument(Map<String, Object> params);
    
    List<String> getOcrDocNoListByUserId(Map<String, Object> params);
    
    List<String> getUserTestFilePathList(Map<String, Object> params);
    
    int deleteUserTestDocuments(Map<String, Object> params);
}
