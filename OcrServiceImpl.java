package com.refine.ocr.service;

import com.refine.ocr.controller.OcrController;
import com.refine.ocr.dao.OcrDAO;
import com.refine.ocr.vo.OcrInfoVO;
import com.refine.rf_core.jdbc.routing.ContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.List;

@Service
public class OcrServiceImpl implements OcrService {

    @Autowired
    private OcrDAO ocrDAO;

    private static final Logger logger = LoggerFactory.getLogger(OcrController.class);

    // 외부 API Base URL 상수
    private static final String DEV_API_BASE_URL = "https://api.work.refinedev.io/apis/refine-ocr-api/v1/application/file";
    private static final String PROD_API_BASE_URL = "https://api.work.refinehub.com/apis/refine-ocr-api/v1/application/file";

    /**
     * 환경에 따른 Base URL 반환
     */
    private String getApiBaseUrl() {
        return "TEST".equals(String.valueOf(ContextHolder.getDbMode())) ? DEV_API_BASE_URL : PROD_API_BASE_URL;
    }



    /**
     * OCR 문서 전체 건수 조회
     *
     * @param params 검색 조건
     * @return 전체 건수
     */
    public int getOcrDocumentCount(Map<String, Object> params) {
        try {
            return ocrDAO.getOcrDocumentCount(params);
        } catch (Exception e) {
            logger.error("OCR 문서 건수 조회 중 오류 발생", e);
            return 0;
        }
    }

    /**
     * OCR 문서 목록 조회
     *
     * @param params 검색 조건
     * @return OCR 문서 목록
     */
    public List<OcrInfoVO> getOcrDocumentList(Map<String, Object> params) {
        validateAndSetDefaults(params);

        try {
            List<OcrInfoVO> list = ocrDAO.getOcrDocumentList(params);

            if (list != null && !list.isEmpty()) {
                for (OcrInfoVO item : list) {
                    String docName = getDocumentName(item.getInst_cd(), item.getPrdt_cd(), item.getDoc_tp_cd());
                    if (docName != null) {
                        item.setDoc_kr_nm(docName);
                    }
                    if(item.getPrdt_cd().equals("OCR") && item.getDoc_tp_cd().equals("01")) {
                        item.setDoc_kr_nm("테스트");
                    }
                }
            }

            return list;

        } catch (Exception e) {
            logger.error("OCR 문서 목록 조회 실패: {}", e.getMessage());
            throw new RuntimeException("데이터베이스 조회 중 오류가 발생했습니다.", e);
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

    @Override
    public OcrInfoVO getOcrDocumentDetail(Map<String, Object> params) {
        try {
            OcrInfoVO detail = ocrDAO.getOcrDocumentDetail(params);

            if (detail == null) {
                throw new RuntimeException("해당 문서를 찾을 수 없습니다.");
            }

            String docName = getDocumentName(detail.getInst_cd(), detail.getPrdt_cd(), detail.getDoc_tp_cd());
            if (docName != null) {
                detail.setDoc_kr_nm(docName);
            }
            if(detail.getPrdt_cd().equals("OCR") && detail.getDoc_tp_cd().equals("01")) {
                detail.setDoc_kr_nm("테스트");
            }

            return detail;

        } catch (Exception e) {
            logger.error("OCR 문서 상세 조회 실패: {}", e.getMessage());
            throw new RuntimeException("데이터베이스 조회 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public List<OcrInfoVO> getDocumentListByCtrlNo(Map<String, Object> params) {
        try {
            List<OcrInfoVO> list = ocrDAO.getDocumentListByCtrlNo(params);

            if (list != null && !list.isEmpty()) {
                for (OcrInfoVO item : list) {
                    String docName = getDocumentName(item.getInst_cd(), item.getPrdt_cd(), item.getDoc_tp_cd());
                    if (docName != null) {
                        item.setDoc_kr_nm(docName);
                    }
                    if(item.getPrdt_cd().equals("OCR") && item.getDoc_tp_cd().equals("01")) {
                        item.setDoc_kr_nm("테스트");
                    }
                }
            }

            return list;
        } catch (Exception e) {
            logger.error("서류 목록 조회 실패: {}", e.getMessage());
            throw new RuntimeException("데이터베이스 조회 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public List<String> getOcrDocNoList(Map<String, Object> params) {
        try {
            return ocrDAO.getOcrDocNoList(params);
        } catch (Exception e) {
            logger.error("OCR 문서 번호 리스트 조회 실패: {}", e.getMessage());
            throw new RuntimeException("데이터베이스 조회 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public List<OcrInfoVO> getOcrResultText(Map<String, Object> params) {
        try {
            return ocrDAO.getOcrResultText(params);
        } catch (Exception e) {
            logger.error("OCR 결과 텍스트 조회 실패: {}", e.getMessage());
            throw new RuntimeException("데이터베이스 조회 중 오류가 발생했습니다.", e);
        }
    }


    @Override
    public Map<String, Object> uploadFileToExternalApi(org.springframework.web.multipart.MultipartFile file, String path) {
        Map<String, Object> result = new HashMap<>();

        try {

            String uploadUrl = getApiBaseUrl() + "/upload-image";

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
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("inst_cd", instCd);
            params.put("prdt_cd", prdtCd);

            String maxCtrlNo = ocrDAO.getMaxCtrlNo(params);

            String nextCtrlNo;
            if (maxCtrlNo == null || maxCtrlNo.isEmpty()) {
                nextCtrlNo = "000001";
            } else {
                int nextNum = Integer.parseInt(maxCtrlNo) + 1;
                nextCtrlNo = String.format("%06d", nextNum);
            }

            return nextCtrlNo;

        } catch (Exception e) {
            logger.error("다음 CTRL_NO 조회 실패: {}", e.getMessage());
            throw new RuntimeException("CTRL_NO 조회 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public int insertOcrDocument(Map<String, Object> params) {
        try {
            return ocrDAO.insertOcrDocument(params);
        } catch (Exception e) {
            logger.error("OCR 문서 등록 실패: {}", e.getMessage());
            throw new RuntimeException("OCR 문서 등록 중 오류가 발생했습니다.", e);
        }
    }


    /**
     * OCR 테스트 히스토리 조회
     *
     * @param params 검색 조건
     * @return OCR 문서 목록
     */
    public List<OcrInfoVO> getOcrHisList(Map<String, Object> params) {
        validateAndSetDefaults(params);

        try {
            return ocrDAO.getOcrHisList(params);
        } catch (Exception e) {
            logger.error("OCR 문서 목록 조회 실패: {}", e.getMessage());
            throw new RuntimeException("데이터베이스 조회 중 오류가 발생했습니다.", e);
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
            //int deleteResult = deleteOcrDocument(ocrDocNo);
            int deleteResult = 0;

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

    /**
     * 외부 API를 통한 파일 삭제
     */
    public void deleteFileFromExternalApi(List<String> filePaths) {
        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            String deleteFileUrl = getApiBaseUrl() + "/delete-file-multi";

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

    @Override
    @Transactional(readOnly = false)
    public int uploadAndInsertOcrDocument(org.springframework.web.multipart.MultipartFile file, String ctrlYr, String instCd, String prdtCd, String nextCtrlNo, String docTpCd, String usrId) {
        try {
            String originalFileName = file.getOriginalFilename();
            String fileExt = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
            String fileNameWithoutExt = originalFileName.substring(0, originalFileName.lastIndexOf("."));

            // S3 경로 생성
            String s3Path = "test/OCR" + ctrlYr + "/" + instCd + "/" + prdtCd + "/" + nextCtrlNo + "/" + originalFileName;

            // 외부 API로 파일 업로드
            Map<String, Object> uploadResult = uploadFileToExternalApi(file, s3Path);

            if ((boolean) uploadResult.get("success")) {
                // DB에 서류 정보 등록
                Map<String, Object> insertParams = new HashMap<>();
                insertParams.put("ctrl_yr", ctrlYr);
                insertParams.put("inst_cd", instCd);
                insertParams.put("prdt_cd", prdtCd);
                insertParams.put("ctrl_no", nextCtrlNo);
                insertParams.put("doc_tp_cd", docTpCd);
                insertParams.put("doc_fl_nm", fileNameWithoutExt);
                insertParams.put("doc_fl_sav_pth_nm", s3Path);
                insertParams.put("doc_fl_ext", fileExt);
                insertParams.put("ins_id", usrId);
                insertParams.put("enc_yn", "N");
                insertParams.put("ocr_yn", "N");
                insertParams.put("menu_cd", "RF_TEST");

                int insertResult = ocrDAO.insertOcrDocument(insertParams);

                if (insertResult > 0) {
                    logger.info("파일 등록 완료 - 파일명: {}", fileNameWithoutExt);
                    return 1;
                } else {
                    logger.warn("파일 DB 등록 실패 - 파일명: {}", fileNameWithoutExt);
                    return 0;
                }
            } else {
                logger.warn("파일 업로드 실패 - 파일명: {}, 사유: {}", fileNameWithoutExt, uploadResult.get("message"));
                return 0;
            }
        } catch (Exception e) {
            logger.error("파일 처리 중 오류 발생", e);
            throw new RuntimeException("파일 처리 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public int updateOcrStatus(Map<String, Object> params) {
        try {
            return ocrDAO.updateOcrStatus(params);
        } catch (Exception e) {
            logger.error("OCR 상태 업데이트 실패: {}", e.getMessage());
            throw new RuntimeException("데이터베이스 업데이트 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public String getDocumentName(String instCd, String prdtCd, String docTpCd) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("inst_cd", instCd);
            params.put("prdt_cd", prdtCd);
            params.put("doc_tp_cd", docTpCd);

            return ocrDAO.getDocumentName(params);

        } catch (Exception e) {
            logger.error("서류 한글명 조회 실패: {}", e.getMessage());
            return null;
        }
    }


    @Override
    public List<com.refine.ocr.vo.OcrItemVO> getOcrItemList(Map<String, Object> params) {
        try {
            return ocrDAO.getOcrItemList(params);
        } catch (Exception e) {
            logger.error("OCR 항목 코드 목록 조회 실패: {}", e.getMessage());
            throw new RuntimeException("데이터베이스 조회 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public int getOcrItemCount(Map<String, Object> params) {
        try {
            return ocrDAO.getOcrItemCount(params);
        } catch (Exception e) {
            logger.error("OCR 항목 코드 건수 조회 실패: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public int insertOcrItem(Map<String, Object> params) {
        try {
            int exists = ocrDAO.checkOcrItemExists(params);
            if (exists > 0) {
                throw new RuntimeException("이미 존재하는 항목코드입니다.");
            }

            return ocrDAO.insertOcrItem(params);
        } catch (Exception e) {
            logger.error("OCR 항목 코드 추가 실패: {}", e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public int updateOcrItem(Map<String, Object> params) {
        try {
            return ocrDAO.updateOcrItem(params);
        } catch (Exception e) {
            logger.error("OCR 항목 코드 수정 실패: {}", e.getMessage());
            throw new RuntimeException("OCR 항목 코드 수정 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public int deleteOcrItem(Map<String, Object> params) {
        try {
            return ocrDAO.deleteOcrItem(params);
        } catch (Exception e) {
            logger.error("OCR 항목 코드 삭제 실패: {}", e.getMessage());
            throw new RuntimeException("OCR 항목 코드 삭제 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public int activateOcrItem(Map<String, Object> params) {
        try {
            return ocrDAO.activateOcrItem(params);
        } catch (Exception e) {
            logger.error("OCR 항목 코드 활성화 실패: {}", e.getMessage());
            throw new RuntimeException("OCR 항목 코드 활성화 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 외부 API에서 이미지 다운로드
     */
    @Override
    public byte[] downloadImageFromExternalApi(String imagePath, String instCd, String prdtCd) throws Exception {
        String encodedPath = java.net.URLEncoder.encode(java.util.Base64.getEncoder().encodeToString(imagePath.getBytes()), "UTF-8");
        String trgtURL = getApiBaseUrl() + "/preview-image-all?instCd=" + instCd + "&prdtCd=" + prdtCd + "&imagePath=" + encodedPath;

        java.net.URL url = new java.net.URL(trgtURL);
        java.net.URLConnection connection = url.openConnection();
        java.io.InputStream is = connection.getInputStream();
        byte[] fileArray = org.apache.commons.io.IOUtils.toByteArray(is);
        is.close();

        return fileArray;
    }

    /**
     * 여러 이미지 다운로드 및 변환 (컨트롤러에서 변환 로직 호출 필요)
     */
    @Override
    public List<String> downloadAndConvertImages(String instCd, String prdtCd, List<Map<String, Object>> imageList) {
        List<String> images = new ArrayList<>();
        Map<String, byte[]> pdfCache = new HashMap<>();

        for (Map<String, Object> imageInfo : imageList) {
            String imagePath = (String) imageInfo.get("image_path");
            String ext = (String) imageInfo.get("ext");
            Boolean isPdf = (Boolean) imageInfo.get("is_pdf");

            if (imagePath == null || ext == null) {
                continue;
            }

            try {
                byte[] fileArray;
                if (isPdf != null && isPdf && pdfCache.containsKey(imagePath)) {
                    fileArray = pdfCache.get(imagePath);
                } else {
                    fileArray = downloadImageFromExternalApi(imagePath, instCd, prdtCd);
                    if (isPdf != null && isPdf) {
                        pdfCache.put(imagePath, fileArray);
                    }
                }

                // Base64 변환은 컨트롤러에서 처리하도록 byte[]를 반환하는 방식으로 변경 필요
                // 현재는 imagePath만 추가
                images.add(imagePath);
            } catch (Exception e) {
                logger.error("이미지 다운로드 실패 [{}]: {}", imagePath, e.getMessage());
                images.add(imagePath);
            }
        }

        return images;
    }
}
