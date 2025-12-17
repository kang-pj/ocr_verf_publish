package com.refine.ocr.service;

import com.refine.ocr.vo.OcrInfoVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface OcrService {

    int getOcrDocumentCount(Map<String, Object> params);

    List<OcrInfoVO> getOcrDocumentList(Map<String, Object> params);

    OcrInfoVO getOcrDocumentDetail(Map<String, Object> params);

    List<OcrInfoVO> getDocumentListByCtrlNo(Map<String, Object> params);

    List<OcrInfoVO> getAllFilesByCtrlNo(Map<String, Object> params);

    List<OcrInfoVO> getOcrResultText(Map<String, Object> params);

    List<String> getOcrDocNoList(Map<String, Object> params);

    java.util.Map<String, Object> uploadFileToExternalApi(MultipartFile file, String path);

    String getNextCtrlNo(String instCd, String prdtCd);

    int insertOcrDocument(java.util.Map<String, Object> params);

    List<OcrInfoVO> getOcrHisList(java.util.Map<String, Object> params);

    boolean deleteOcrDocumentWithFile(java.util.Map<String, Object> params);

    int deleteUserTestData(String usrId);

    List<String> getUserTestFileList(String usrId);

    void deleteFileFromExternalApi(java.util.List<String> filePaths);

    int uploadAndInsertOcrDocument(org.springframework.web.multipart.MultipartFile file, String ctrlYr, String instCd, String prdtCd, String nextCtrlNo, String docTpCd, String usrId);

    int updateOcrStatus(Map<String, Object> params);

    String getDocumentName(String instCd, String prdtCd, String docTpCd);
    
    Map<String, String> getDocumentNamesBatch(List<OcrInfoVO> docList);

    List<com.refine.ocr.vo.OcrItemVO> getOcrItemList(Map<String, Object> params);

    int getOcrItemCount(Map<String, Object> params);

    int insertOcrItem(Map<String, Object> params);

    int updateOcrItem(Map<String, Object> params);

    int deleteOcrItem(Map<String, Object> params);

    int activateOcrItem(Map<String, Object> params);

    // 외부 API 이미지 다운로드
    byte[] downloadImageFromExternalApi(String imagePath, String instCd, String prdtCd) throws Exception;

    // 문서 유형 목록 조회
    List<Map<String, Object>> getDocumentTypes(Map<String, Object> params);

    // 여러 이미지 다운로드 및 변환
    List<String> downloadAndConvertImages(String instCd, String prdtCd, List<Map<String, Object>> imageList);

}