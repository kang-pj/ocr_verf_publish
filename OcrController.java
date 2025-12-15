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
     * OCR 결과 목록 조회
     */
    @PostMapping(value = "/api/getOcrResultList.do")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getOcrResultList(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            int totalCount = ocrService.getOcrDocumentCount(params);
            List<OcrInfoVO> list = ocrService.getOcrDocumentList(params);

            result.put("success", true);
            result.put("data", list);
            result.put("recordsTotal", totalCount);
            result.put("recordsFiltered", totalCount);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);

        } catch (Exception e) {
            logger.error("OCR 결과 목록 조회 실패: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "데이터 조회 중 오류가 발생했습니다.");

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
            // 필수 파라미터 검증
            String ctrlYr = (String) params.get("ctrl_yr");
            String instCd = (String) params.get("inst_cd");
            String prdtCd = (String) params.get("prdt_cd");
            String ctrlNo = (String) params.get("ctrl_no");

            if (ctrlYr == null || instCd == null || prdtCd == null || ctrlNo == null) {
                throw new IllegalArgumentException("관리번호 정보가 필요합니다.");
            }

            // OCR 문서 번호 리스트 조회
            List<String> originalOcrDocNoList = ocrService.getOcrDocNoList(params);

            if (originalOcrDocNoList == null || originalOcrDocNoList.isEmpty()) {
                throw new RuntimeException("해당 조건의 문서가 없습니다.");
            }

            // 현재 조회할 ocr_doc_no 결정
            String currentOcrDocNo = (String) params.get("ocr_doc_no");
            if (currentOcrDocNo == null || currentOcrDocNo.isEmpty()) {
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
                    String fileName = docInfo.getDoc_fl_nm();
                    
                    logger.info("OCR_DOC_NO: {}, DOC_FL_NM: {}, EXT: {}", ocrDocNo, fileName, ext);

                    if ("pdf".equalsIgnoreCase(ext)) {
                        int pageCount = getPdfPageCount(docInfo.getInst_cd(), docInfo.getPrdt_cd(), docInfo.getDoc_fl_sav_pth_nm());

                        for (int i = 0; i < pageCount; i++) {
                            expandedOcrDocNoList.add(ocrDocNo);

                            Map<String, Object> imageInfo = new HashMap<>();
                            imageInfo.put("ocr_doc_no", ocrDocNo);
                            imageInfo.put("image_path", docInfo.getDoc_fl_sav_pth_nm());
                            imageInfo.put("ext", ext);
                            imageInfo.put("file_name", fileName);
                            imageInfo.put("inst_cd", docInfo.getInst_cd());
                            imageInfo.put("prdt_cd", docInfo.getPrdt_cd());
                            imageInfo.put("page_number", i + 1);
                            imageInfo.put("is_pdf", true);
                            imageInfoList.add(imageInfo);
                        }
                    } else {
                        expandedOcrDocNoList.add(ocrDocNo);

                        Map<String, Object> imageInfo = new HashMap<>();
                        imageInfo.put("ocr_doc_no", ocrDocNo);
                        imageInfo.put("image_path", docInfo.getDoc_fl_sav_pth_nm());
                        imageInfo.put("ext", ext);
                        imageInfo.put("file_name", fileName);
                        imageInfo.put("inst_cd", docInfo.getInst_cd());
                        imageInfo.put("prdt_cd", docInfo.getPrdt_cd());
                        imageInfo.put("page_number", 1);
                        imageInfo.put("is_pdf", false);
                        imageInfoList.add(imageInfo);
                    }
                }
            }

            // 확장된 리스트에서 현재 인덱스 찾기
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

            // OCR 결과 텍스트 조회
            Map<String, Object> ocrParams = new HashMap<>();
            ocrParams.put("ocr_doc_no", currentOcrDocNo);
            List<OcrInfoVO> ocrResults = ocrService.getOcrResultText(ocrParams);

            result.put("success", true);
            result.put("data", detail);
            result.put("documentList", documentList);
            result.put("ocrResults", ocrResults);
            result.put("ocrDocNoList", expandedOcrDocNoList);
            result.put("imageInfoList", imageInfoList);
            result.put("currentIndex", currentIndex);
            result.put("totalPages", expandedOcrDocNoList.size());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);

        } catch (IllegalArgumentException e) {
            logger.error("OCR 문서 상세 조회 실패 - 파라미터 오류: {}", e.getMessage());
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
        } catch (Exception e) {
            logger.error("OCR 문서 상세 조회 실패: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "문서 조회 중 오류가 발생했습니다.");
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
        }
    }









    /**
     * 이미지 파일 서빙
     */
    @RequestMapping(value = "/image/**")
    public ResponseEntity<byte[]> getImage(javax.servlet.http.HttpServletRequest request) {
        try {
            String requestUrl = request.getRequestURI();
            String filePath = requestUrl.substring(requestUrl.indexOf("/image/") + 7);

            java.io.File file = new java.io.File("/path/to/ocr/images/" + filePath);

            if (!file.exists()) {
                logger.warn("이미지 파일 없음: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            byte[] imageBytes = java.nio.file.Files.readAllBytes(file.toPath());

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
            logger.error("이미지 로딩 실패: {}", e.getMessage());
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

            try {
                byte[] fileArray = ocrService.downloadImageFromExternalApi(imagePath, instCd, prdtCd);
                String base64Image = convertToBase64Image(fileArray, ext);

                result.put("success", true);
                result.put("data", base64Image);

            } catch (Exception e) {
                logger.warn("외부 API 이미지 로딩 실패: {}", e.getMessage());
                result.put("success", true);
                result.put("data", imagePath);
            }

            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);

        } catch (Exception e) {
            logger.error("이미지 로딩 실패: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "이미지 로딩 중 오류가 발생했습니다.");
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
        }
    }

    /**
     * 여러 OCR 문서의 이미지 정보 조회
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

            List<Map<String, Object>> imageInfoList = new ArrayList<>();
            for (String ocrDocNo : ocrDocNoList) {
                Map<String, Object> docParams = new HashMap<>();
                docParams.put("ocr_doc_no", ocrDocNo);

                OcrInfoVO docInfo = ocrService.getOcrDocumentDetail(docParams);
                if (docInfo != null && docInfo.getDoc_fl_sav_pth_nm() != null) {
                    String ext = docInfo.getDoc_fl_ext();

                    if ("pdf".equalsIgnoreCase(ext)) {
                        int pageCount = getPdfPageCount(docInfo.getInst_cd(), docInfo.getPrdt_cd(), docInfo.getDoc_fl_sav_pth_nm());

                        for (int i = 0; i < pageCount; i++) {
                            Map<String, Object> imageInfo = new HashMap<>();
                            imageInfo.put("ocr_doc_no", ocrDocNo);
                            imageInfo.put("image_path", docInfo.getDoc_fl_sav_pth_nm());
                            imageInfo.put("ext", ext);
                            imageInfo.put("file_name", docInfo.getDoc_fl_nm());
                            imageInfo.put("inst_cd", docInfo.getInst_cd());
                            imageInfo.put("prdt_cd", docInfo.getPrdt_cd());
                            imageInfo.put("page_number", i + 1);
                            imageInfo.put("is_pdf", true);
                            imageInfoList.add(imageInfo);
                        }
                    } else {
                        Map<String, Object> imageInfo = new HashMap<>();
                        imageInfo.put("ocr_doc_no", ocrDocNo);
                        imageInfo.put("image_path", docInfo.getDoc_fl_sav_pth_nm());
                        imageInfo.put("ext", ext);
                        imageInfo.put("file_name", docInfo.getDoc_fl_nm());
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

            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);

        } catch (Exception e) {
            logger.error("OCR 문서 이미지 정보 조회 실패: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "이미지 정보 조회 중 오류가 발생했습니다.");
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
            Map<String, byte[]> pdfCache = new HashMap<>();

            for (Map<String, Object> imageInfo : imageList) {
                String imagePath = (String) imageInfo.get("image_path");
                String ext = (String) imageInfo.get("ext");
                Boolean isPdf = (Boolean) imageInfo.get("is_pdf");
                Integer pageNumber = (Integer) imageInfo.get("page_number");

                if (imagePath == null || ext == null) {
                    continue;
                }

                try {
                    byte[] fileArray;
                    if (isPdf != null && isPdf && pdfCache.containsKey(imagePath)) {
                        fileArray = pdfCache.get(imagePath);
                    } else {
                        // imagePath는 클라이언트에서 이미 Base64 인코딩되어 전달됨
                        fileArray = ((OcrServiceImpl) ocrService).downloadImageFromExternalApi(imagePath, instCd, prdtCd, true);
                        if (isPdf != null && isPdf) {
                            pdfCache.put(imagePath, fileArray);
                        }
                    }

                    String base64Image;
                    if (isPdf != null && isPdf && pageNumber != null) {
                        base64Image = convertToBase64Image(fileArray, ext, pageNumber);
                    } else {
                        base64Image = convertToBase64Image(fileArray, ext);
                    }

                    images.add(base64Image);
                } catch (Exception e) {
                    logger.error("이미지 로딩 실패 [{}]: {}", imagePath, e.getMessage());
                    // 에러 정보를 포함한 객체를 추가 (클라이언트에서 구분 가능하도록)
                    images.add("ERROR: 파일을 불러올 수 없습니다 - " + e.getMessage());
                }
            }

            result.put("success", true);
            result.put("data", images);

            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);

        } catch (Exception e) {
            logger.error("여러 이미지 로딩 실패: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "이미지 로딩 중 오류가 발생했습니다.");
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
        byte[] outputArray = fileArray;

        try {
            if ("pdf".equalsIgnoreCase(ext)) {
                PDDocument document = PDDocument.load(fileArray);
                int totalPages = document.getNumberOfPages();
                int pageIndex = pageNumber - 1;

                if (pageIndex < 0 || pageIndex >= totalPages) {
                    pageIndex = 0;
                }

                BufferedImage image = new PDFRenderer(document).renderImageWithDPI(pageIndex, 300);
                document.close();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "JPEG", baos);
                outputArray = baos.toByteArray();
                mimeType = "image/jpeg";
            } else if ("png".equalsIgnoreCase(ext)) {
                mimeType = "image/png";
                outputArray = fileArray;
            } else if ("gif".equalsIgnoreCase(ext)) {
                mimeType = "image/gif";
                outputArray = fileArray;
            } else if ("tif".equalsIgnoreCase(ext) || "tiff".equalsIgnoreCase(ext)) {
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
                mimeType = "image/jpeg";
                outputArray = fileArray;
            }
        } catch (Exception e) {
            logger.warn("이미지 변환 실패: {}", e.getMessage());
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
        
        // 운영 환경에서는 접근 불가
        String dbMode = System.getProperty("DBMODE");
        if ("PROD".equalsIgnoreCase(dbMode) || "REAL".equalsIgnoreCase(dbMode)) {
            result.put("success", false);
            result.put("message", "운영 환경에서는 사용할 수 없는 기능입니다.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }
        
        List<Map<String, Object>> uploadedFiles = new ArrayList<>();
        int totalInserted = 0;
        int totalFailed = 0;

        try {
            if (files == null || files.length == 0) {
                result.put("success", false);
                result.put("message", "파일이 없습니다.");
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
            }

            String nextCtrlNo = ocrService.getNextCtrlNo(instCd, prdtCd);
            LoginVO loginVO = (LoginVO) session.getAttribute("USER");
            String usrId = (loginVO != null) ? loginVO.getUsrId() : "SYSTEM";

            for (org.springframework.web.multipart.MultipartFile file : files) {
                if (file.isEmpty()) {
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
                    logger.error("파일 처리 실패 [{}]: {}", file.getOriginalFilename(), e.getMessage());
                }
            }

            if (totalInserted > 0) {
                result.put("success", true);
                result.put("message", totalInserted + "개 파일이 등록되었습니다.");
                if (totalFailed > 0) {
                    result.put("message", result.get("message") + " (" + totalFailed + "개 실패)");
                }
                result.put("data", uploadedFiles);
                result.put("totalInserted", totalInserted);
                result.put("totalFailed", totalFailed);
                logger.info("OCR 테스트 완료 - 성공: {}, 실패: {}", totalInserted, totalFailed);
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
            logger.error("OCR 테스트 실패: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "서류 등록 중 오류가 발생했습니다.");

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
            LoginVO loginVO = (LoginVO) session.getAttribute("USER");
            if (loginVO == null) {
                result.put("success", false);
                result.put("message", "로그인 정보가 없습니다.");
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
            }

            String usrId = loginVO.getUsrId();

            Map<String, Object> params = new HashMap<>();
            params.put("inst_cd", "99");
            params.put("prdt_cd", "OCR");
            params.put("ins_id", usrId);

            List<OcrInfoVO> historyList = ocrService.getOcrHisList(params);

            result.put("success", true);
            result.put("data", historyList);
            result.put("recordsTotal", historyList.size());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);

        } catch (Exception e) {
            logger.error("OCR 테스트 히스토리 조회 실패: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "히스토리 조회 중 오류가 발생했습니다.");

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
            List<OcrInfoVO> ocrResults = ocrService.getOcrResultText(params);

            result.put("success", true);
            result.put("data", ocrResults);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);

        } catch (Exception e) {
            logger.error("OCR 결과 텍스트 조회 실패: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "OCR 결과 조회 중 오류가 발생했습니다.");

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
            LoginVO loginVO = (LoginVO) session.getAttribute("USER");
            if (loginVO == null) {
                result.put("success", false);
                result.put("message", "로그인 정보가 없습니다.");
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
            }

            String usrId = loginVO.getUsrId();

            List<String> filePathList = ocrService.getUserTestFileList(usrId);

            if (filePathList != null && !filePathList.isEmpty()) {
                try {
                    ocrService.deleteFileFromExternalApi(filePathList);
                } catch (Exception e) {
                    logger.warn("파일 삭제 API 실패 (DB 삭제는 진행): {}", e.getMessage());
                }
            }

            int deleteCount = ocrService.deleteUserTestData(usrId);

            if (deleteCount > 0) {
                result.put("success", true);
                result.put("message", deleteCount + "개의 테스트 데이터가 삭제되었습니다.");
                result.put("data", new HashMap<String, Object>() {{
                    put("delete_count", deleteCount);
                    put("delete_time", System.currentTimeMillis());
                }});
                logger.info("히스토리 삭제 완료 - 사용자: {}, 삭제: {}", usrId, deleteCount);
            } else {
                result.put("success", false);
                result.put("message", "삭제할 테스트 데이터가 없습니다.");
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);

        } catch (Exception e) {
            logger.error("히스토리 삭제 실패: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "히스토리 삭제 중 오류가 발생했습니다.");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);
        }
    }

    /**
     * 파일 다운로드 (단일)
     */
    @GetMapping(value = "/api/downloadFile.do")
    public void downloadFile(
            @RequestParam("inst_cd") String instCd,
            @RequestParam("prdt_cd") String prdtCd,
            @RequestParam("image_path") String imagePath,
            @RequestParam("file_name") String fileName,
            @RequestParam("ext") String ext,
            javax.servlet.http.HttpServletResponse response) {
        
        try {
            logger.info("파일 다운로드 요청 - 파일명: {}", fileName);
            
            byte[] fileData = ocrService.downloadImageFromExternalApi(imagePath, instCd, prdtCd);
            
            String contentType;
            if ("pdf".equalsIgnoreCase(ext)) {
                contentType = "application/pdf";
            } else if ("png".equalsIgnoreCase(ext)) {
                contentType = "image/png";
            } else if ("gif".equalsIgnoreCase(ext)) {
                contentType = "image/gif";
            } else if ("tif".equalsIgnoreCase(ext) || "tiff".equalsIgnoreCase(ext)) {
                contentType = "image/tiff";
            } else {
                contentType = "image/jpeg";
            }
            
            String encodedFileName = URLEncoder.encode(fileName + "." + ext, "UTF-8").replaceAll("\\+", "%20");
            
            response.setContentType(contentType);
            response.setContentLength(fileData.length);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
            
            java.io.OutputStream out = response.getOutputStream();
            out.write(fileData);
            out.flush();
            out.close();
            
            logger.info("파일 다운로드 완료 - 파일명: {}, 크기: {} bytes", fileName, fileData.length);
            
        } catch (Exception e) {
            logger.error("파일 다운로드 실패: {}", e.getMessage());
            try {
                response.sendError(javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "파일 다운로드 실패");
            } catch (IOException ex) {
                logger.error("에러 응답 전송 실패", ex);
            }
        }
    }

    /**
     * 전체 파일 다운로드 (ZIP)
     */
    @PostMapping(value = "/api/downloadAllFiles.do")
    public void downloadAllFiles(
            @RequestParam("ctrl_yr") String ctrlYr,
            @RequestParam("inst_cd") String instCd,
            @RequestParam("prdt_cd") String prdtCd,
            @RequestParam("ctrl_no") String ctrlNo,
            javax.servlet.http.HttpServletResponse response) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("ctrl_yr", ctrlYr);
        params.put("inst_cd", instCd);
        params.put("prdt_cd", prdtCd);
        params.put("ctrl_no", ctrlNo);
        
        try {
            logger.info("전체 파일 다운로드 요청 - 관리번호: {}-{}-{}-{}", ctrlYr, instCd, prdtCd, ctrlNo);

            // 해당 관리번호의 모든 파일 조회
            List<OcrInfoVO> documentList = ocrService.getAllFilesByCtrlNo(params);
            
            logger.info("조회된 파일 개수: {}", documentList != null ? documentList.size() : 0);

            if (documentList == null || documentList.isEmpty()) {
                logger.warn("다운로드할 파일이 없습니다.");
                response.sendError(javax.servlet.http.HttpServletResponse.SC_NOT_FOUND, "다운로드할 파일이 없습니다.");
                return;
            }

            // ZIP 파일 생성
            response.setContentType("application/zip");
            String zipFileName = URLEncoder.encode(ctrlYr + "-" + instCd + "-" + prdtCd + "-" + ctrlNo + ".zip", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + zipFileName + "\"");

            java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(response.getOutputStream());

            int fileCount = 0;
            for (OcrInfoVO doc : documentList) {
                try {
                    String imagePath = doc.getDoc_fl_sav_pth_nm();
                    String docFileName = doc.getDoc_fl_nm();
                    String docExt = doc.getDoc_fl_ext();
                    String docTpCd = doc.getDoc_tp_cd();
                    String ocrDocNo = doc.getOcr_doc_no();

                    logger.info("처리 중인 파일: {} (경로: {})", docFileName, imagePath);

                    if (imagePath == null || imagePath.isEmpty()) {
                        logger.warn("파일 경로가 없습니다: {}", docFileName);
                        continue;
                    }

                    // 외부 API에서 파일 다운로드
                    logger.info("외부 API 호출 시작: {}", imagePath);
                    byte[] fileData = ocrService.downloadImageFromExternalApi(imagePath, instCd, prdtCd);
                    logger.info("파일 다운로드 완료: {} bytes", fileData.length);

                    // ZIP 엔트리 파일명 생성 (doc_fl_nm이 없으면 doc_tp_cd + OCR_DOC_NO 사용)
                    String entryName;
                    if (docFileName != null && !docFileName.isEmpty()) {
                        entryName = docFileName + "." + docExt;
                    } else {
                        entryName = docTpCd + "_" + ocrDocNo + "." + docExt;
                    }
                    
                    java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(entryName);
                    zos.putNextEntry(zipEntry);
                    zos.write(fileData);
                    zos.closeEntry();

                    fileCount++;
                    logger.info("ZIP에 파일 추가: {}", entryName);

                } catch (Exception e) {
                    logger.error("파일 추가 실패 [{}]: {}", doc.getDoc_fl_nm(), e.getMessage(), e);
                }
            }

            zos.finish();
            zos.close();

            logger.info("전체 파일 다운로드 완료 - {} 개 파일", fileCount);

        } catch (Exception e) {
            logger.error("전체 파일 다운로드 실패: {}", e.getMessage(), e);
            try {
                response.sendError(javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "파일 다운로드 실패");
            } catch (IOException ex) {
                logger.error("에러 응답 전송 실패", ex);
            }
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

            Map<String, Object> updateParams = new HashMap<>();
            updateParams.put("ocr_doc_no", ocrDocNo);
            updateParams.put("ocr_yn", ocrYn);

            int updateResult = ocrService.updateOcrStatus(updateParams);

            if (updateResult > 0) {
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(ApiResponse.success("OCR 상태가 업데이트되었습니다.").toMap());
            } else {
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(ApiResponse.fail("OCR 상태 업데이트에 실패했습니다.").toMap());
            }

        } catch (Exception e) {
            logger.error("OCR 상태 업데이트 실패: {}", e.getMessage());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ApiResponse.fail("OCR 상태 업데이트 중 오류가 발생했습니다.").toMap());
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
            List<com.refine.ocr.vo.OcrItemVO> list = ocrService.getOcrItemList(params);
            int totalCount = ocrService.getOcrItemCount(params);

            result.put("success", true);
            result.put("data", list);
            result.put("totalCount", totalCount);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);

        } catch (Exception e) {
            logger.error("OCR 항목 코드 목록 조회 실패: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "항목 코드 조회 중 오류가 발생했습니다.");

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
            String userId = "SYSTEM";
            Object loginVO = session.getAttribute("USER");
            if (loginVO != null) {
                try {
                    java.lang.reflect.Method method = loginVO.getClass().getMethod("getUsrId");
                    userId = (String) method.invoke(loginVO);
                } catch (Exception e) {
                    logger.warn("사용자 ID 조회 실패, SYSTEM 사용");
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
            logger.error("OCR 항목 코드 추가 실패: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "항목 코드 추가 중 오류가 발생했습니다.");

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
            String userId = "SYSTEM";
            Object loginVO = session.getAttribute("USER");
            if (loginVO != null) {
                try {
                    java.lang.reflect.Method method = loginVO.getClass().getMethod("getUsrId");
                    userId = (String) method.invoke(loginVO);
                } catch (Exception e) {
                    logger.warn("사용자 ID 조회 실패, SYSTEM 사용");
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
            logger.error("OCR 항목 코드 수정 실패: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "항목 코드 수정 중 오류가 발생했습니다.");

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
            String userId = "SYSTEM";
            Object loginVO = session.getAttribute("USER");
            if (loginVO != null) {
                try {
                    java.lang.reflect.Method method = loginVO.getClass().getMethod("getUsrId");
                    userId = (String) method.invoke(loginVO);
                } catch (Exception e) {
                    logger.warn("사용자 ID 조회 실패, SYSTEM 사용");
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
            logger.error("OCR 항목 코드 삭제 실패: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "항목 코드 삭제 중 오류가 발생했습니다.");

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
            String userId = "SYSTEM";
            Object loginVO = session.getAttribute("USER");
            if (loginVO != null) {
                try {
                    java.lang.reflect.Method method = loginVO.getClass().getMethod("getUsrId");
                    userId = (String) method.invoke(loginVO);
                } catch (Exception e) {
                    logger.warn("사용자 ID 조회 실패, SYSTEM 사용");
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
            logger.error("OCR 항목 코드 활성화 실패: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "항목 코드 활성화 중 오류가 발생했습니다.");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);
        }
    }

}
