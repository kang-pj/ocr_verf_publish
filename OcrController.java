package com.refine.ocr.controller;

import com.refine.common.code.service.CodeService;
import com.refine.login.vo.LoginVO;
import com.refine.ocr.dao.OcrDAO;
import com.refine.ocr.service.OcrService;
import com.refine.ocr.vo.OcrInfoVO;
import com.refine.rf_core.jdbc.routing.ContextHolder;
import com.refine.util.ApiResponse;
import com.refine.util.HttpUtil;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.List;
import java.util.Base64;


@Controller
public class OcrController {

    @Autowired
    protected OcrService ocrService;
    private OcrDAO ocrDAO;

    @Autowired
    protected CodeService codeService;

    private static final Logger logger = LoggerFactory.getLogger(OcrController.class);

    /**
     * OCR 목록 조회 페이지 호출
     */
    @RequestMapping(value = "/{site}/ocrRsltList", method = RequestMethod.GET)
    public String ocrRsltList(@PathVariable("site") String site, Map<String, Object> modelMap, HttpServletRequest request) throws Exception {
        modelMap.put("site", site);

        return "ocr/ocrRsltList";
    }

    /**
     * OCR 상세 페이지 호출
     */
    @RequestMapping(value = "/{site}/ocrRsltDetail", method = RequestMethod.GET)
    public String ocrRsltDetail(@PathVariable("site") String site, Map<String, Object> modelMap, HttpServletRequest request) throws Exception {
        modelMap.put("site", site);

        return "ocr/ocrRsltDetail";
    }

    /**
     * OCR 목록 조회 페이지 호출
     */
    @RequestMapping(value = "/{site}/ocrRealtimeTest", method = RequestMethod.GET)
    public String ocrTest(@PathVariable("site") String site, Map<String, Object> modelMap, HttpServletRequest request) throws Exception {
        modelMap.put("site", site);

        return "ocr/ocrRealtimeTest";
    }

    /**
     * OCR Item 조회 페이지 호출
     */
    @RequestMapping(value = "/{site}/ocrItemCtrl", method = RequestMethod.GET)
    public String ocrItemCtrl(@PathVariable("site") String site, Map<String, Object> modelMap, HttpServletRequest request) throws Exception {
        modelMap.put("site", site);

        return "ocr/ocrItemCtrl";
    }

    /**
     * OCR 결과 목록 조회 (List)
     *
     * @param params 검색 조건
     * @return JSON 응답
     */
    @PostMapping(value = "/api/getOcrResultList.do")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getOcrResultList(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("OCR 결과 목록 조회 요청: {}", params);

            int totalCount = ocrService.getOcrDocumentCount(params);
            // 데이터 조회 (Service에서 검증 및 기본값 설정)
            List<OcrInfoVO> list = ocrService.getOcrDocumentList(params);

            result.put("success", true);
            result.put("data", list);
            result.put("recordsTotal", totalCount);
            result.put("recordsFiltered", totalCount);

            logger.info("OCR 결과 목록 조회 완료: {} 건", list.size());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);

        } catch (Exception e) {
            logger.error("OCR 결과 목록 조회 실패", e);
            result.put("success", false);
            result.put("message", "데이터 조회 중 오류가 발생했습니다: " + e.getMessage());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);
        }
    }

    /**
     * OCR 문서 상세 조회
     */
    @PostMapping(value = "/api/getOcrDocumentDetail.do")
    public ResponseEntity<Map<String, Object>> getOcrDocumentDetail(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("OCR 문서 상세 조회 요청: {}", params);

            // 필수 파라미터 검증
            String ctrlYr = (String) params.get("ctrl_yr");
            String instCd = (String) params.get("inst_cd");
            String prdtCd = (String) params.get("prdt_cd");
            String ctrlNo = (String) params.get("ctrl_no");

            if (ctrlYr == null || instCd == null || prdtCd == null || ctrlNo == null) {
                throw new IllegalArgumentException("관리번호 정보가 필요합니다.");
            }

            // OCR 문서 번호 리스트 조회 (원본)
            List<String> originalOcrDocNoList = ocrService.getOcrDocNoList(params);

            if (originalOcrDocNoList == null || originalOcrDocNoList.isEmpty()) {
                throw new RuntimeException("해당 조건의 문서가 없습니다.");
            }

            // 현재 조회할 ocr_doc_no 결정
            String currentOcrDocNo = (String) params.get("ocr_doc_no");
            if (currentOcrDocNo == null || currentOcrDocNo.isEmpty()) {
                // ocr_doc_no가 없으면 첫 번째 문서
                currentOcrDocNo = originalOcrDocNoList.get(0);
            }

            // PDF 페이지 수를 고려한 확장된 리스트 및 이미지 정보 생성
            List<String> expandedOcrDocNoList = new ArrayList<>();
            List<Map<String, Object>> imageInfoList = new ArrayList<>();

            for (String ocrDocNo : originalOcrDocNoList) {
                Map<String, Object> docParams = new HashMap<>();
                docParams.put("ocr_doc_no", ocrDocNo);

                OcrInfoVO docInfo = ocrService.getOcrDocumentDetail(docParams);
                if (docInfo != null && docInfo.getDoc_fl_sav_pth_nm() != null) {
                    String ext = docInfo.getDoc_fl_ext();

                    if ("pdf".equalsIgnoreCase(ext)) {
                        // PDF인 경우 페이지 수만큼 추가
                        int pageCount = getPdfPageCount(docInfo.getInst_cd(), docInfo.getPrdt_cd(), docInfo.getDoc_fl_sav_pth_nm());
                        logger.info("PDF 페이지 수: {} - OCR_DOC_NO: {}", pageCount, ocrDocNo);

                        for (int i = 0; i < pageCount; i++) {
                            expandedOcrDocNoList.add(ocrDocNo);

                            Map<String, Object> imageInfo = new HashMap<>();
                            imageInfo.put("ocr_doc_no", ocrDocNo);
                            imageInfo.put("image_path", docInfo.getDoc_fl_sav_pth_nm());
                            imageInfo.put("ext", ext);
                            imageInfo.put("inst_cd", docInfo.getInst_cd());
                            imageInfo.put("prdt_cd", docInfo.getPrdt_cd());
                            imageInfo.put("page_number", i + 1);
                            imageInfo.put("is_pdf", true);
                            imageInfoList.add(imageInfo);
                        }
                    } else {
                        // 이미지는 1개만 추가
                        expandedOcrDocNoList.add(ocrDocNo);

                        Map<String, Object> imageInfo = new HashMap<>();
                        imageInfo.put("ocr_doc_no", ocrDocNo);
                        imageInfo.put("image_path", docInfo.getDoc_fl_sav_pth_nm());
                        imageInfo.put("ext", ext);
                        imageInfo.put("inst_cd", docInfo.getInst_cd());
                        imageInfo.put("prdt_cd", docInfo.getPrdt_cd());
                        imageInfo.put("page_number", 1);
                        imageInfo.put("is_pdf", false);
                        imageInfoList.add(imageInfo);
                    }
                }
            }

            // 확장된 리스트에서 현재 인덱스 찾기 (첫 번째 매칭)
            int currentIndex = expandedOcrDocNoList.indexOf(currentOcrDocNo);
            if (currentIndex == -1) {
                currentIndex = 0;
                currentOcrDocNo = expandedOcrDocNoList.get(0);
            }

            // 상세 정보 조회
            Map<String, Object> detailParams = new HashMap<>();
            detailParams.put("ocr_doc_no", currentOcrDocNo);
            OcrInfoVO detail = ocrService.getOcrDocumentDetail(detailParams);

            // 같은 관리번호의 서류 목록 조회
            List<OcrInfoVO> documentList = ocrService.getDocumentListByCtrlNo(params);

            // 서류 목록 로그 출력
            if (documentList != null && !documentList.isEmpty()) {
                logger.info("서류 목록 개수: {}", documentList.size());
                for (OcrInfoVO doc : documentList) {
                    logger.info("서류 - DOC_TP_CD: {}, DOC_KR_NM: {}, DOC_TITLE: {}",
                            doc.getDoc_tp_cd(), doc.getDoc_kr_nm(), doc.getDoc_title());
                }
            }

            // OCR 결과 텍스트 조회
            Map<String, Object> ocrParams = new HashMap<>();
            ocrParams.put("ocr_doc_no", currentOcrDocNo);
            List<OcrInfoVO> ocrResults = ocrService.getOcrResultText(ocrParams);

            logger.info("OCR 결과 조회 - OCR_DOC_NO: {}, 현재 인덱스: {}/{}",
                    currentOcrDocNo, currentIndex + 1, expandedOcrDocNoList.size());

            logger.info("OCR 결과 개수: {}", ocrResults != null ? ocrResults.size() : 0);
            if (ocrResults != null && !ocrResults.isEmpty()) {
                OcrInfoVO first = ocrResults.get(0);
                logger.info("첫 번째 OCR 결과 - ITEM_CD: {}, ITEM_NM: {}, ITEM_VALUE: {}",
                        first.getItem_cd(), first.getItem_nm(), first.getItem_value());
            }

            result.put("success", true);
            result.put("data", detail);
            result.put("documentList", documentList);
            result.put("ocrResults", ocrResults);
            result.put("ocrDocNoList", expandedOcrDocNoList);
            result.put("imageInfoList", imageInfoList);
            result.put("currentIndex", currentIndex);
            result.put("totalPages", expandedOcrDocNoList.size());

            logger.info("OCR 문서 상세 조회 완료: {}-{}-{}-{}", ctrlYr, instCd, prdtCd, ctrlNo);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);

        } catch (Exception e) {
            logger.error("OCR 문서 상세 조회 실패", e);
            result.put("success", false);
            result.put("message", e.getMessage());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);
        }
    }









    /**
     * 이미지 파일 서빙
     */
    @RequestMapping(value = "/image/**")
    public ResponseEntity<byte[]> getImage(javax.servlet.http.HttpServletRequest request) {
        try {
            // URL에서 파일 경로 추출
            String requestUrl = request.getRequestURI();
            String filePath = requestUrl.substring(requestUrl.indexOf("/image/") + 7);

            logger.info("이미지 요청: {}", filePath);

            // 실제 파일 시스템 경로 (환경에 맞게 수정 필요)
            java.io.File file = new java.io.File("/path/to/ocr/images/" + filePath);

            if (!file.exists()) {
                logger.warn("이미지 파일을 찾을 수 없음: {}", file.getAbsolutePath());
                return ResponseEntity.notFound().build();
            }

            // 파일 읽기
            byte[] imageBytes = java.nio.file.Files.readAllBytes(file.toPath());

            // Content-Type 설정
            String contentType = "image/jpeg";
            if (filePath.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            } else if (filePath.toLowerCase().endsWith(".gif")) {
                contentType = "image/gif";
            } else if (filePath.toLowerCase().endsWith(".pdf")) {
                contentType = "application/pdf";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageBytes);

        } catch (Exception e) {
            logger.error("이미지 로딩 실패", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 이미지 로딩 및 변환
     */
    @PostMapping(value = "/api/getOcrImage.do")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getOcrImage(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            String instCd = (String) params.get("inst_cd");
            String prdtCd = (String) params.get("prdt_cd");
            String imagePath = (String) params.get("image_path");
            String ext = (String) params.get("ext");

            if (imagePath == null || ext == null) {
                result.put("success", false);
                result.put("message", "이미지 경로와 확장자가 필요합니다.");
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
            }

            // 이미지 URL 구성
            String baseUrl = "TEST".equals(String.valueOf(ContextHolder.getDbMode()))
                    ? "https://api.work.refinedev.io/apis/refine-ocr-api/v1/application/file/preview-image-all"
                    : "https://api.work.refinehub.com/apis/refine-ocr-api/v1/application/file/preview-image-all";

            String encodedPath = URLEncoder.encode(Base64.getEncoder().encodeToString(imagePath.getBytes()), "UTF-8");
            String trgtURL = baseUrl + "?instCd=" + instCd + "&prdtCd=" + prdtCd + "&imagePath=" + encodedPath;

            try {
                logger.debug("이미지 호출 <UNK>: {}", trgtURL);
                // 이미지 다운로드
                InputStream is = HttpUtil.httpConnectionStream(trgtURL, "GET", null, null);
                byte[] fileArray = IOUtils.toByteArray(is);
                is.close();

                // 포맷별 변환
                String base64Image = convertToBase64Image(fileArray, ext);

                result.put("success", true);
                result.put("data", base64Image);

            } catch (Exception e) {
                logger.warn("외부 API 이미지 로딩 실패, 경로만 반환: {}", e.getMessage());
                result.put("success", true);
                result.put("data", imagePath);
            }

            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);

        } catch (Exception e) {
            logger.error("이미지 로딩 실패", e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
        }
    }

    /**
     * 여러 OCR 문서의 이미지 정보 조회 (ocr_doc_no 리스트 기반)
     */
    @PostMapping(value = "/api/getOcrDocumentImages.do")
    public ResponseEntity<Map<String, Object>> getOcrDocumentImages(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<String> ocrDocNoList = (List<String>) params.get("ocr_doc_no_list");

            if (ocrDocNoList == null || ocrDocNoList.isEmpty()) {
                result.put("success", false);
                result.put("message", "OCR 문서 번호 리스트가 필요합니다.");
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
            }

            logger.info("OCR 문서 이미지 정보 조회 - 문서 개수: {}", ocrDocNoList.size());

            // 각 ocr_doc_no의 이미지 정보 조회
            List<Map<String, Object>> imageInfoList = new ArrayList<>();
            for (String ocrDocNo : ocrDocNoList) {
                Map<String, Object> docParams = new HashMap<>();
                docParams.put("ocr_doc_no", ocrDocNo);

                OcrInfoVO docInfo = ocrService.getOcrDocumentDetail(docParams);
                if (docInfo != null && docInfo.getDoc_fl_sav_pth_nm() != null) {
                    String ext = docInfo.getDoc_fl_ext();

                    // PDF인 경우 페이지 수만큼 이미지 정보 추가
                    if ("pdf".equalsIgnoreCase(ext)) {
                        // PDF 페이지 수 조회
                        int pageCount = getPdfPageCount(docInfo.getInst_cd(), docInfo.getPrdt_cd(), docInfo.getDoc_fl_sav_pth_nm());

                        logger.info("PDF 페이지 수: {} - OCR_DOC_NO: {}", pageCount, ocrDocNo);

                        for (int i = 0; i < pageCount; i++) {
                            Map<String, Object> imageInfo = new HashMap<>();
                            imageInfo.put("ocr_doc_no", ocrDocNo);
                            imageInfo.put("image_path", docInfo.getDoc_fl_sav_pth_nm());
                            imageInfo.put("ext", ext);
                            imageInfo.put("inst_cd", docInfo.getInst_cd());
                            imageInfo.put("prdt_cd", docInfo.getPrdt_cd());
                            imageInfo.put("page_number", i + 1);
                            imageInfo.put("is_pdf", true);
                            imageInfoList.add(imageInfo);
                        }
                    } else {
                        // 이미지 파일은 1개만 추가
                        Map<String, Object> imageInfo = new HashMap<>();
                        imageInfo.put("ocr_doc_no", ocrDocNo);
                        imageInfo.put("image_path", docInfo.getDoc_fl_sav_pth_nm());
                        imageInfo.put("ext", ext);
                        imageInfo.put("inst_cd", docInfo.getInst_cd());
                        imageInfo.put("prdt_cd", docInfo.getPrdt_cd());
                        imageInfo.put("page_number", 1);
                        imageInfo.put("is_pdf", false);
                        imageInfoList.add(imageInfo);
                    }
                }
            }

            result.put("success", true);
            result.put("data", imageInfoList);

            logger.info("OCR 문서 이미지 정보 조회 완료: {} 건", imageInfoList.size());

            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);

        } catch (Exception e) {
            logger.error("OCR 문서 이미지 정보 조회 실패", e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
        }
    }

    /**
     * 여러 이미지 로딩 (Viewer.js용)
     */
    @PostMapping(value = "/api/getOcrImages.do")
    public ResponseEntity<Map<String, Object>> getOcrImages(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            String instCd = (String) params.get("inst_cd");
            String prdtCd = (String) params.get("prdt_cd");
            List<Map<String, Object>> imageList = (List<Map<String, Object>>) params.get("image_list");

            if (imageList == null || imageList.isEmpty()) {
                result.put("success", false);
                result.put("message", "이미지 목록이 필요합니다.");
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
            }

            List<String> images = new ArrayList<>();
            // 이미지 URL 구성
            String baseUrl = "TEST".equals(String.valueOf(ContextHolder.getDbMode()))
                    ? "https://api.work.refinedev.io/apis/refine-ocr-api/v1/application/file/preview-image-all"
                    : "https://api.work.refinehub.com/apis/refine-ocr-api/v1/application/file/preview-image-all";


            // PDF 파일 캐싱을 위한 맵 (같은 파일을 여러 번 다운로드하지 않도록)
            Map<String, byte[]> pdfCache = new HashMap<>();

            for (Map<String, Object> imageInfo : imageList) {
                String imagePath = (String) imageInfo.get("image_path");
                String ext = (String) imageInfo.get("ext");
                Boolean isPdf = (Boolean) imageInfo.get("is_pdf");
                Integer pageNumber = (Integer) imageInfo.get("page_number");

                logger.info("이미지 정보 - PATH: {}, EXT: {}, IS_PDF: {}, PAGE: {}",
                        imagePath, ext, isPdf, pageNumber);

                if (imagePath == null || ext == null) {
                    continue;
                }

                try {
                    String encodedPath = URLEncoder.encode(Base64.getEncoder().encodeToString(imagePath.getBytes()), "UTF-8");
                    String trgtURL = baseUrl + "?instCd=" + instCd + "&prdtCd=" + prdtCd + "&imagePath=" + encodedPath;

                    // 캐시 확인 (PDF인 경우)
                    byte[] fileArray;
                    if (isPdf != null && isPdf && pdfCache.containsKey(imagePath)) {
                        logger.info("PDF 캐시 사용: {}", imagePath);
                        fileArray = pdfCache.get(imagePath);
                    } else {
                        // 외부 API에서 파일 다운로드
                        logger.info("파일 다운로드: {}", imagePath);
                        fileArray = downloadImageFromUrl(trgtURL);

                        // PDF인 경우 캐시에 저장
                        if (isPdf != null && isPdf) {
                            pdfCache.put(imagePath, fileArray);
                        }
                    }

                    // PDF인 경우 페이지 번호를 사용하여 변환
                    String base64Image;
                    if (isPdf != null && isPdf && pageNumber != null) {
                        logger.info("PDF 페이지 변환 시작: 페이지 {}", pageNumber);
                        base64Image = convertToBase64Image(fileArray, ext, pageNumber);
                        logger.info("PDF 페이지 변환 완료: 페이지 {}", pageNumber);
                    } else {
                        logger.info("이미지 변환 (PDF 아님)");
                        base64Image = convertToBase64Image(fileArray, ext);
                    }

                    images.add(base64Image);
                } catch (Exception e) {
                    logger.error("이미지 로딩 실패: {} - {}", imagePath, e.getMessage(), e);
                    images.add(imagePath);
                }
            }

            result.put("success", true);
            result.put("data", images);

            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);

        } catch (Exception e) {
            logger.error("여러 이미지 로딩 실패", e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
        }
    }


    /**
     * URL에서 이미지 다운로드
     */
    private byte[] downloadImageFromUrl(String urlString) throws IOException {
        java.net.URL url = new java.net.URL(urlString);
        java.net.URLConnection connection = url.openConnection();
        InputStream is = connection.getInputStream();
        byte[] fileArray = IOUtils.toByteArray(is);
        is.close();
        return fileArray;
    }

    /**
     * 이미지를 Base64로 변환
     */
    private String convertToBase64Image(byte[] fileArray, String ext) throws IOException {
        return convertToBase64Image(fileArray, ext, 1);
    }
    /**
     * 이미지를 Base64로 변환 (페이지 번호 지정 가능)
     */
    private String convertToBase64Image(byte[] fileArray, String ext, int pageNumber) throws IOException {
        String mimeType = "image/jpeg";
        String outputFormat = "JPEG";
        byte[] outputArray = fileArray;

        try {
            if ("pdf".equalsIgnoreCase(ext)) {
                PDDocument document = PDDocument.load(fileArray);
                int totalPages = document.getNumberOfPages();
                int pageIndex = pageNumber - 1; // 0-based index

                logger.info("PDF 변환 - 요청 페이지: {}, 전체 페이지: {}, 인덱스: {}",
                        pageNumber, totalPages, pageIndex);

                // 페이지 번호 유효성 검사
                if (pageIndex < 0 || pageIndex >= totalPages) {
                    logger.warn("잘못된 페이지 번호, 첫 페이지로 변경: {} -> 0", pageIndex);
                    pageIndex = 0;
                }

                BufferedImage image = new PDFRenderer(document).renderImageWithDPI(pageIndex, 300);
                document.close();

                logger.info("PDF 페이지 {} 렌더링 완료", pageIndex + 1);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "JPEG", baos);
                outputArray = baos.toByteArray();
                mimeType = "image/jpeg";
            } else if ("png".equalsIgnoreCase(ext)) {
                // PNG는 원본 유지
                mimeType = "image/png";
                outputArray = fileArray;
            } else if ("gif".equalsIgnoreCase(ext)) {
                // GIF는 원본 유지
                mimeType = "image/gif";
                outputArray = fileArray;
            } else if ("tif".equalsIgnoreCase(ext) || "tiff".equalsIgnoreCase(ext)) {
                // TIFF를 JPEG로 변환
                BufferedImage original = ImageIO.read(new ByteArrayInputStream(fileArray));
                if (original != null) {
                    BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
                    image.createGraphics().drawImage(original, 0, 0, Color.WHITE, null);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(image, "JPEG", baos);
                    outputArray = baos.toByteArray();
                    mimeType = "image/jpeg";
                }
            } else {
                // JPG 및 기타 형식은 원본 유지
                mimeType = "image/jpeg";
                outputArray = fileArray;
            }
        } catch (Exception e) {
            logger.warn("이미지 변환 실패, 원본 반환: {}", e.getMessage());
            outputArray = fileArray;
        }

        return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(outputArray);
    }

    /**
     * OCR 실시간 테스트 (서류 등록)
     */
    @PostMapping(value = "/api/ocrRealtimeTest.do", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> ocrRealtimeTest(
            @RequestParam("instCd") String instCd,
            @RequestParam("prdtCd") String prdtCd,
            @RequestParam("ctrlYr") String ctrlYr,
            @RequestParam("docTpCd") String docTpCd,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile[] files,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> uploadedFiles = new ArrayList<>();
        int totalInserted = 0;
        int totalFailed = 0;

        try {
            logger.info("OCR 실시간 테스트 요청 - 파일 개수: {}", files.length);

            if (files == null || files.length == 0) {
                result.put("success", false);
                result.put("message", "파일이 없습니다.");
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
            }

            // DB에서 다음 ctrl_no 조회
            String nextCtrlNo = ocrService.getNextCtrlNo(instCd, prdtCd);

            // 로그인 정보 조회
            LoginVO loginVO = (LoginVO) session.getAttribute("USER");
            String usrId = (loginVO != null) ? loginVO.getUsrId() : "SYSTEM";

            // 각 파일 처리
            for (org.springframework.web.multipart.MultipartFile file : files) {
                if (file.isEmpty()) {
                    logger.warn("빈 파일 스킵");
                    totalFailed++;
                    continue;
                }

                try {
                    int uploadRes = ocrService.uploadAndInsertOcrDocument(file, ctrlYr, instCd, prdtCd, nextCtrlNo, docTpCd, usrId);

                    if (uploadRes > 0) {
                        totalInserted++;
                        Map<String, Object> fileInfo = new HashMap<>();
                        fileInfo.put("file_name", file.getOriginalFilename());
                        uploadedFiles.add(fileInfo);
                    } else {
                        totalFailed++;
                    }
                } catch (Exception e) {
                    totalFailed++;
                    logger.error("파일 처리 중 오류 발생", e);
                }
            }

            // 결과 반환
            if (totalInserted > 0) {
                result.put("success", true);
                result.put("message", totalInserted + "개 파일이 등록되었습니다.");
                if (totalFailed > 0) {
                    result.put("message", result.get("message") + " (" + totalFailed + "개 실패)");
                }
                result.put("data", uploadedFiles);
                result.put("totalInserted", totalInserted);
                result.put("totalFailed", totalFailed);
                logger.info("OCR 실시간 테스트 완료 - 성공: {} 건, 실패: {} 건", totalInserted, totalFailed);
            } else {
                result.put("success", false);
                result.put("message", "등록된 파일이 없습니다.");
                result.put("totalInserted", 0);
                result.put("totalFailed", totalFailed);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);

        } catch (Exception e) {
            logger.error("OCR 실시간 테스트 실패", e);
            result.put("success", false);
            result.put("message", "서류 등록 중 오류가 발생했습니다: " + e.getMessage());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);
        }
    }



    /**
     * OCR 실시간 테스트 히스토리 목록 조회
     */
    @PostMapping(value = "/api/getOcrTestHistory.do")
    public ResponseEntity<Map<String, Object>> getOcrTestHistory(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 로그인 정보 조회
            LoginVO loginVO = (LoginVO) session.getAttribute("USER");
            if (loginVO == null) {
                result.put("success", false);
                result.put("message", "로그인 정보가 없습니다.");
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
            }

            String usrId = loginVO.getUsrId();
            logger.info("OCR 테스트 히스토리 조회 요청 - 사용자: {}", usrId);

            // 조회 조건 설정
            Map<String, Object> params = new HashMap<>();
            params.put("inst_cd", "99");
            params.put("prdt_cd", "OCR");
            params.put("ins_id", usrId);

            // 히스토리 목록 조회 (최신순)
            List<OcrInfoVO> historyList = ocrService.getOcrHisList(params);

            result.put("success", true);
            result.put("data", historyList);
            result.put("recordsTotal", historyList.size());

            logger.info("OCR 테스트 히스토리 조회 완료: {} 건", historyList.size());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);

        } catch (Exception e) {
            logger.error("OCR 테스트 히스토리 조회 실패", e);
            result.put("success", false);
            result.put("message", "히스토리 조회 중 오류가 발생했습니다: " + e.getMessage());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);
        }
    }


    /**
     * OCR 결과 텍스트 조회
     */
    @PostMapping(value = "/api/getOcrResultText.do")
    public ResponseEntity<Map<String, Object>> getOcrResultText(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("OCR 결과 텍스트 조회 요청: {}", params);

            // OCR 결과 조회
            List<OcrInfoVO> ocrResults = ocrService.getOcrResultText(params);

            result.put("success", true);
            result.put("data", ocrResults);

            logger.info("OCR 결과 텍스트 조회 완료: {} 건", ocrResults != null ? ocrResults.size() : 0);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);

        } catch (Exception e) {
            logger.error("OCR 결과 텍스트 조회 실패", e);
            result.put("success", false);
            result.put("message", "OCR 결과 조회 중 오류가 발생했습니다: " + e.getMessage());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);
        }
    }



    /**
     * 히스토리 삭제
     */
    @PostMapping(value = "/api/deleteHistory.do")
    public ResponseEntity<Map<String, Object>> deleteHistory(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 로그인 정보 조회
            LoginVO loginVO = (LoginVO) session.getAttribute("USER");
            if (loginVO == null) {
                result.put("success", false);
                result.put("message", "로그인 정보가 없습니다.");
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
            }

            String usrId = loginVO.getUsrId();
            logger.info("히스토리 삭제 요청 - 사용자: {}", usrId);

            // 1. 현재 사용자의 파일 리스트 조회
            List<String> filePathList = ocrService.getUserTestFileList(usrId);

            // 2. 파일 삭제 API 호출
            if (filePathList != null && !filePathList.isEmpty()) {
                try {
                    ocrService.deleteFileFromExternalApi(filePathList);
                    logger.info("파일 삭제 API 호출 완료 - 파일 개수: {}", filePathList.size());
                } catch (Exception e) {
                    logger.warn("파일 삭제 API 호출 실패 (DB 삭제는 진행됨): {}", e.getMessage());
                    // 파일 삭제 실패해도 DB 삭제는 진행
                }
            }

            // 3. Service에서 현재 사용자의 테스트 데이터 삭제
            int deleteCount = ocrService.deleteUserTestData(usrId);

            if (deleteCount > 0) {
                result.put("success", true);
                result.put("message", deleteCount + "개의 테스트 데이터가 삭제되었습니다.");
                result.put("data", new HashMap<String, Object>() {{
                    put("delete_count", deleteCount);
                    put("delete_time", System.currentTimeMillis());
                }});

                logger.info("히스토리 삭제 완료 - 사용자: {}, 삭제 건수: {}", usrId, deleteCount);
            } else {
                result.put("success", false);
                result.put("message", "삭제할 테스트 데이터가 없습니다.");
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);

        } catch (Exception e) {
            logger.error("히스토리 삭제 실패", e);
            result.put("success", false);
            result.put("message", "히스토리 삭제 중 오류가 발생했습니다: " + e.getMessage());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);
        }
    }

    /**
     * OCR 상태 업데이트 (ocr_yn 변경)
     */
    @PostMapping(value = "/api/updateOcrStatus.do")
    public ResponseEntity<Map<String, Object>> updateOcrStatus(@RequestBody Map<String, Object> params) {
        try {
            String ocrDocNo = (String) params.get("ocr_doc_no");
            String ocrYn = (String) params.get("ocr_yn");

            if (ocrDocNo == null || ocrDocNo.isEmpty()) {
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(ApiResponse.fail("OCR 문서 번호가 필요합니다.").toMap());
            }

            if (ocrYn == null || ocrYn.isEmpty()) {
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(ApiResponse.fail("OCR 상태 값이 필요합니다.").toMap());
            }

            logger.info("OCR 상태 업데이트 요청 - OCR_DOC_NO: {}, OCR_YN: {}", ocrDocNo, ocrYn);

            // OCR 상태 업데이트
            Map<String, Object> updateParams = new HashMap<>();
            updateParams.put("ocr_doc_no", ocrDocNo);
            updateParams.put("ocr_yn", ocrYn);

            int updateResult = ocrService.updateOcrStatus(updateParams);

            if (updateResult > 0) {
                logger.info("OCR 상태 업데이트 완료 - OCR_DOC_NO: {}", ocrDocNo);
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(ApiResponse.success("OCR 상태가 업데이트되었습니다.").toMap());
            } else {
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(ApiResponse.fail("OCR 상태 업데이트에 실패했습니다.").toMap());
            }

        } catch (Exception e) {
            logger.error("OCR 상태 업데이트 실패", e);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ApiResponse.fail("OCR 상태 업데이트 중 오류가 발생했습니다: " + e.getMessage()).toMap());
        }
    }

    /**
     * PDF 페이지 수 조회
     */
    private int getPdfPageCount(String instCd, String prdtCd, String imagePath) {
        try {
            String baseUrl = "TEST".equals(String.valueOf(ContextHolder.getDbMode()))
                    ? "https://api.work.refinedev.io/apis/refine-ocr-api/v1/application/file/preview-image-all"
                    : "https://api.work.refinehub.com/apis/refine-ocr-api/v1/application/file/preview-image-all";
            String encodedPath = URLEncoder.encode(Base64.getEncoder().encodeToString(imagePath.getBytes()), "UTF-8");
            String trgtURL = baseUrl + "?instCd=" + instCd + "&prdtCd=" + prdtCd + "&imagePath=" + encodedPath;

            byte[] fileArray = downloadImageFromUrl(trgtURL);

            PDDocument document = PDDocument.load(fileArray);
            int pageCount = document.getNumberOfPages();
            document.close();

            return pageCount;
        } catch (Exception e) {
            logger.warn("PDF 페이지 수 조회 실패, 기본값 1 반환: {}", e.getMessage());
            return 1;
        }
    }


    /**
     * OCR 항목 코드 목록 조회
     */
    @PostMapping(value = "/api/getOcrItemList.do")
    public ResponseEntity<Map<String, Object>> getOcrItemList(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("OCR 항목 코드 목록 조회 요청: {}", params);

            // 목록 조회
            List<com.refine.ocr.vo.OcrItemVO> list = ocrService.getOcrItemList(params);

            // 건수 조회
            int totalCount = ocrService.getOcrItemCount(params);

            result.put("success", true);
            result.put("data", list);
            result.put("totalCount", totalCount);

            logger.info("OCR 항목 코드 목록 조회 완료: {} 건", list != null ? list.size() : 0);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);

        } catch (Exception e) {
            logger.error("OCR 항목 코드 목록 조회 실패", e);
            result.put("success", false);
            result.put("message", e.getMessage());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);
        }
    }


    /**
     * OCR 항목 코드 추가
     */
    @PostMapping(value = "/api/insertOcrItem.do")
    public ResponseEntity<Map<String, Object>> insertOcrItem(@RequestBody Map<String, Object> params, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("OCR 항목 코드 추가 요청: {}", params);

            // 세션에서 사용자 ID 가져오기
            String userId = "SYSTEM";
            Object loginVO = session.getAttribute("USER");
            if (loginVO != null) {
                try {
                    // LoginVO에서 usrId 가져오기
                    java.lang.reflect.Method method = loginVO.getClass().getMethod("getUsrId");
                    userId = (String) method.invoke(loginVO);
                } catch (Exception e) {
                    logger.warn("LoginVO에서 usrId 가져오기 실패, SYSTEM 사용", e);
                }
            }

            params.put("insr_id", userId);
            params.put("updr_id", userId);

            int insertResult = ocrService.insertOcrItem(params);

            result.put("success", insertResult > 0);
            result.put("message", insertResult > 0 ? "추가되었습니다." : "추가 실패");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);

        } catch (Exception e) {
            logger.error("OCR 항목 코드 추가 실패", e);
            result.put("success", false);
            result.put("message", e.getMessage());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);
        }
    }

    /**
     * OCR 항목 코드 수정
     */
    @PostMapping(value = "/api/updateOcrItem.do")
    public ResponseEntity<Map<String, Object>> updateOcrItem(@RequestBody Map<String, Object> params, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("OCR 항목 코드 수정 요청: {}", params);

            // 세션에서 사용자 ID 가져오기
            String userId = "SYSTEM";
            Object loginVO = session.getAttribute("USER");
            if (loginVO != null) {
                try {
                    java.lang.reflect.Method method = loginVO.getClass().getMethod("getUsrId");
                    userId = (String) method.invoke(loginVO);
                } catch (Exception e) {
                    logger.warn("LoginVO에서 usrId 가져오기 실패, SYSTEM 사용", e);
                }
            }

            params.put("updr_id", userId);

            int updateResult = ocrService.updateOcrItem(params);

            result.put("success", updateResult > 0);
            result.put("message", updateResult > 0 ? "수정되었습니다." : "수정 실패");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);

        } catch (Exception e) {
            logger.error("OCR 항목 코드 수정 실패", e);
            result.put("success", false);
            result.put("message", e.getMessage());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);
        }
    }

    /**
     * OCR 항목 코드 삭제 (use_yn = N)
     */
    @PostMapping(value = "/api/deleteOcrItem.do")
    public ResponseEntity<Map<String, Object>> deleteOcrItem(@RequestBody Map<String, Object> params, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("OCR 항목 코드 삭제 요청: {}", params);

            // 세션에서 사용자 ID 가져오기
            String userId = "SYSTEM";
            Object loginVO = session.getAttribute("USER");
            if (loginVO != null) {
                try {
                    java.lang.reflect.Method method = loginVO.getClass().getMethod("getUsrId");
                    userId = (String) method.invoke(loginVO);
                } catch (Exception e) {
                    logger.warn("LoginVO에서 usrId 가져오기 실패, SYSTEM 사용", e);
                }
            }

            params.put("updr_id", userId);

            int deleteResult = ocrService.deleteOcrItem(params);

            result.put("success", deleteResult > 0);
            result.put("message", deleteResult > 0 ? "삭제되었습니다." : "삭제 실패");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);

        } catch (Exception e) {
            logger.error("OCR 항목 코드 삭제 실패", e);
            result.put("success", false);
            result.put("message", e.getMessage());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);
        }
    }

    /**
     * OCR 항목 코드 활성화 (use_yn = Y)
     */
    @PostMapping(value = "/api/activateOcrItem.do")
    public ResponseEntity<Map<String, Object>> activateOcrItem(@RequestBody Map<String, Object> params, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("OCR 항목 코드 활성화 요청: {}", params);

            // 세션에서 사용자 ID 가져오기
            String userId = "SYSTEM";
            Object loginVO = session.getAttribute("USER");
            if (loginVO != null) {
                try {
                    java.lang.reflect.Method method = loginVO.getClass().getMethod("getUsrId");
                    userId = (String) method.invoke(loginVO);
                } catch (Exception e) {
                    logger.warn("LoginVO에서 usrId 가져오기 실패, SYSTEM 사용", e);
                }
            }

            params.put("updr_id", userId);

            int activateResult = ocrService.activateOcrItem(params);

            result.put("success", activateResult > 0);
            result.put("message", activateResult > 0 ? "활성화되었습니다." : "활성화 실패");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);

        } catch (Exception e) {
            logger.error("OCR 항목 코드 활성화 실패", e);
            result.put("success", false);
            result.put("message", e.getMessage());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);
        }
    }

}
