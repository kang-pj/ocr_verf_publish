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
    
    int updateOcrStatus(Map<String, Object> params);
    
    OcrInfoVO getImageInfo(Map<String, Object> params);
    
    byte[] getOcrImage(String instCd, String prdtCd, String imagePath);
    
    String getSysClsCd(String instCd, String prdtCd);
    
    void uploadFileToS3(org.springframework.web.multipart.MultipartFile file, String path);

    java.util.Map<String, Object> uploadFileToExternalApi(org.springframework.web.multipart.MultipartFile file, String path);
    
    String getNextCtrlNo(String instCd, String prdtCd);
    
    int insertOcrDocument(java.util.Map<String, Object> params);
    
    int deleteOcrDocument(String ocrDocNo);
    
    boolean deleteOcrDocumentWithFile(java.util.Map<String, Object> params);
    
    java.util.List<String> getUserTestFileList(String usrId);
    
    void deleteFileFromExternalApi(java.util.List<String> filePaths);
    
    int uploadAndInsertOcrDocument(org.springframework.web.multipart.MultipartFile file, String ctrlYr, String instCd, String prdtCd, String nextCtrlNo, String docTpCd, String usrId);
    
    int deleteUserTestData(String usrId);
}
