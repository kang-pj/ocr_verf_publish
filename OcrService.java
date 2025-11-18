package com.refine.ocr.service;

import java.util.List;
import java.util.Map;

import com.refine.ocr.vo.OcrInfoVO;

public interface OcrService {
    
    int getOcrDocumentCount(Map<String, Object> params);
    
    List<OcrInfoVO> getOcrDocumentList(Map<String, Object> params);
    
    OcrInfoVO getOcrDocumentDetail(Map<String, Object> params);
    
    List<OcrInfoVO> getDocumentListByCtrlNo(Map<String, Object> params);
    
    List<OcrInfoVO> getOcrResultText(Map<String, Object> params);
    
    List<String> getOcrDocNoList(Map<String, Object> params);
    
    int updateVerificationStatus(Map<String, Object> params);
    
    OcrInfoVO getImageInfo(Map<String, Object> params);
}
