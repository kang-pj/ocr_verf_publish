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
    
    @Override
    public void uploadFileToS3(org.springframework.web.multipart.MultipartFile file, String path) {
        logger.info("S3 파일 업로드 시작 - PATH: {}", path);
        
        try {
            s3Util.uploadFile(file, path);
            logger.info("S3 파일 업로드 완료 - PATH: {}", path);
        } catch (Exception e) {
            logger.error("S3 파일 업로드 실패 - PATH: {}", path, e);
            throw new RuntimeException("S3 파일 업로드 중 오류가 발생했습니다.", e);
        }
    }
    
    @Override
    public Map<String, Object> uploadFileToExternalApi(org.springframework.web.multipart.MultipartFile file, String path) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String uploadUrl = "https://api.work.refinedev.io/apis/refine-ocr-api/v1/application/file/upload-image";
            
            // RestTemplate 사용
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            
            // MultiValueMap으로 form-data 구성
            org.springframework.util.MultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
            body.add("file", new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
            body.add("path", path);
            
            // HttpHeaders 설정
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);
            
            // HttpEntity 생성
            org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, Object>> requestEntity = 
                new org.springframework.http.HttpEntity<>(body, headers);
            
            // 외부 API 호출
            org.springframework.http.ResponseEntity<Map> response = restTemplate.postForEntity(uploadUrl, requestEntity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                result.put("success", true);
                result.put("message", "파일 업로드 성공");
                result.put("data", response.getBody());
            } else {
                result.put("success", false);
                result.put("message", "파일 업로드 실패");
            }
            
            logger.info("외부 API 파일 업로드 완료 - PATH: {}", path);
            
        } catch (Exception e) {
            logger.error("외부 API 파일 업로드 실패", e);
            result.put("success", false);
            result.put("message", "파일 업로드 중 오류: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public String getNextCtrlNo(String instCd, String prdtCd) {
        logger.debug("다음 CTRL_NO 조회: INST_CD={}, PRDT_CD={}", instCd, prdtCd);
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("inst_cd", instCd);
            params.put("prdt_cd", prdtCd);
            
            // DB에서 최대 CTRL_NO 조회
            String maxCtrlNo = ocrDAO.getMaxCtrlNo(params);
            
            String nextCtrlNo;
            if (maxCtrlNo == null || maxCtrlNo.isEmpty()) {
                // 첫 번째 데이터
                nextCtrlNo = "000001";
            } else {
                // 최대값 + 1
                int nextNum = Integer.parseInt(maxCtrlNo) + 1;
                nextCtrlNo = String.format("%06d", nextNum);
            }
            
            logger.debug("다음 CTRL_NO: {}", nextCtrlNo);
            return nextCtrlNo;
            
        } catch (Exception e) {
            logger.error("다음 CTRL_NO 조회 실패", e);
            throw new RuntimeException("CTRL_NO 조회 중 오류가 발생했습니다.", e);
        }
    }
    
    @Override
    public int insertOcrDocument(Map<String, Object> params) {
        logger.info("OCR 문서 등록: CTRL_YR={}, INST_CD={}, PRDT_CD={}, CTRL_NO={}", 
            params.get("ctrl_yr"), params.get("inst_cd"), params.get("prdt_cd"), params.get("ctrl_no"));
        
        try {
            int result = ocrDAO.insertOcrDocument(params);
            logger.info("OCR 문서 등록 완료: {} 건", result);
            return result;
        } catch (Exception e) {
            logger.error("OCR 문서 등록 실패", e);
            throw new RuntimeException("OCR 문서 등록 중 오류가 발생했습니다.", e);
        }
    }
    
    @Override
    @Transactional(readOnly = false)
    public int deleteOcrDocument(String ocrDocNo) {
        logger.info("OCR 문서 삭제: OCR_DOC_NO={}", ocrDocNo);
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("ocr_doc_no", ocrDocNo);
            
            int result = ocrDAO.deleteOcrDocument(params);
            logger.info("OCR 문서 삭제 완료: {} 건", result);
            return result;
        } catch (Exception e) {
            logger.error("OCR 문서 삭제 실패", e);
            throw new RuntimeException("OCR 문서 삭제 중 오류가 발생했습니다.", e);
        }
    }
    
    @Override
    @Transactional(readOnly = false)
    public boolean deleteOcrDocumentWithFile(Map<String, Object> params) {
        String ocrDocNo = (String) params.get("ocr_doc_no");
        String docFlSavPthNm = (String) params.get("doc_fl_sav_pth_nm");
        
        logger.info("OCR 문서 및 파일 삭제 시작: OCR_DOC_NO={}", ocrDocNo);
        
        try {
            // 1. DB에서 삭제
            int deleteResult = deleteOcrDocument(ocrDocNo);
            
            if (deleteResult > 0) {
                logger.info("DB 삭제 완료 - OCR_DOC_NO: {}", ocrDocNo);
                
                // 2. 파일 삭제 API 호출 (외부 프로젝트)
                if (docFlSavPthNm != null && !docFlSavPthNm.isEmpty()) {
                    try {
                        List<String> filePaths = new ArrayList<>();
                        filePaths.add(docFlSavPthNm);
                        deleteFileFromExternalApi(filePaths);
                        logger.info("파일 삭제 API 호출 완료 - PATH: {}", docFlSavPthNm);
                    } catch (Exception e) {
                        logger.warn("파일 삭제 API 호출 실패 (DB 삭제는 완료됨): {}", e.getMessage());
                        // 파일 삭제 실패해도 DB 삭제는 완료되었으므로 성공으로 처리
                    }
                }
                
                return true;
            } else {
                logger.warn("삭제할 히스토리가 없음 - OCR_DOC_NO: {}", ocrDocNo);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("OCR 문서 및 파일 삭제 실패", e);
            throw new RuntimeException("OCR 문서 삭제 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 외부 API를 통한 파일 삭제
     */
    public void deleteFileFromExternalApi(List<String> filePaths) {
        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            String deleteFileUrl = "http://localhost:8080/delete-files";
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            
            Map<String, List<String>> requestBody = new HashMap<>();
            requestBody.put("filePaths", filePaths);
            
            org.springframework.http.HttpEntity<Map<String, List<String>>> requestEntity = 
                new org.springframework.http.HttpEntity<>(requestBody, headers);
            
            restTemplate.postForEntity(deleteFileUrl, requestEntity, String.class);
            
            logger.info("파일 삭제 API 호출 완료 - 파일 개수: {}", filePaths.size());
        } catch (Exception e) {
            logger.error("파일 삭제 API 호출 실패", e);
            throw new RuntimeException("파일 삭제 중 오류가 발생했습니다.", e);
        }
    }
    
    @Override
    public List<String> getUserTestFileList(String usrId) {
        logger.info("사용자 테스트 파일 리스트 조회: USR_ID={}", usrId);
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("ins_id", usrId);
            params.put("inst_cd", "99");
            params.put("prdt_cd", "OCR");
            
            // 사용자의 테스트 파일 경로 리스트 조회
            List<String> filePathList = ocrDAO.getUserTestFilePathList(params);
            
            logger.info("사용자 테스트 파일 리스트 조회 완료: USR_ID={}, 파일 개수={}", usrId, 
                filePathList != null ? filePathList.size() : 0);
            
            return filePathList;
            
        } catch (Exception e) {
            logger.error("사용자 테스트 파일 리스트 조회 실패: USR_ID={}", usrId, e);
            throw new RuntimeException("파일 리스트 조회 중 오류가 발생했습니다.", e);
        }
    }
    
    @Override
    @Transactional(readOnly = false)
    public int deleteUserTestData(String usrId) {
        logger.info("사용자 테스트 데이터 삭제 시작: USR_ID={}", usrId);
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("ins_id", usrId);
            params.put("inst_cd", "99");
            params.put("prdt_cd", "OCR");
            
            // 1. 삭제할 ocr_doc_no 리스트 조회
            List<String> ocrDocNoList = ocrDAO.getOcrDocNoListByUserId(params);
            
            if (ocrDocNoList == null || ocrDocNoList.isEmpty()) {
                logger.info("삭제할 테스트 데이터 없음: USR_ID={}", usrId);
                return 0;
            }
            
            logger.info("삭제 대상 문서 개수: {}", ocrDocNoList.size());
            
            // 2. 각 문서의 결과 데이터 삭제
            int totalResultDeleted = 0;
            for (String ocrDocNo : ocrDocNoList) {
                Map<String, Object> deleteParams = new HashMap<>();
                deleteParams.put("ocr_doc_no", ocrDocNo);
                int resultDeleted = ocrDAO.deleteOcrResult(deleteParams);
                totalResultDeleted += resultDeleted;
                logger.debug("OCR 결과 삭제: OCR_DOC_NO={}, 삭제 건수={}", ocrDocNo, resultDeleted);
            }
            
            // 3. 문서 정보 삭제
            int docDeleted = ocrDAO.deleteUserTestDocuments(params);
            logger.info("사용자 테스트 데이터 삭제 완료: USR_ID={}, 결과 삭제={}, 문서 삭제={}", 
                usrId, totalResultDeleted, docDeleted);
            
            return docDeleted;
            
        } catch (Exception e) {
            logger.error("사용자 테스트 데이터 삭제 실패: USR_ID={}", usrId, e);
            throw new RuntimeException("테스트 데이터 삭제 중 오류가 발생했습니다.", e);
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
