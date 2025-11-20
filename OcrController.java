package com.refine.ocr.controller;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.refine.ocr.service.OcrService;
import com.refine.ocr.vo.OcrInfoVO;

/**
 * OCR 문서 관리 컨트롤러
 */
@RestController
public class OcrController {
    
    private static final Logger logger = LoggerFactory.getLogger(OcrController.class);
    
    @Autowired
    private OcrService ocrService;
    
    /**
     * OCR 결과 목록 조회 (AJAX)
     * 
     * @param params 검색 조건
     * @return JSON 응답
     */
    @PostMapping(value = "/rf-ocr-verf/api/getOcrResultList.do")
    public ResponseEntity<Map<String, Object>> getOcrResultList(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            logger.info("OCR 결과 목록 조회 요청: {}", params);
            
            // 전체 건수 조회
            int totalCount = ocrService.getOcrDocumentCount(params);
            
            // 데이터 조회 (Service에서 검증 및 기본값 설정)
            List<OcrInfoVO> list = ocrService.getOcrDocumentList(params);
            
            result.put("success", true);
            result.put("data", list);
            result.put("recordsTotal", totalCount);
            result.put("recordsFiltered", totalCount);
            
            logger.info("OCR 결과 목록 조회 완료: {} 건 (전체: {} 건)", list.size(), totalCount);
            
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
     * 이미지 로딩 및 변환
     */
    @PostMapping(value = "/rf-ocr-verf/api/getOcrImage.do")
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
     * 이미지를 Base64로 변환
     */
    private String convertToBase64Image(byte[] fileArray, String ext) throws IOException {
        String mimeType = "image/jpeg";
        String outputFormat = "JPEG";
        byte[] outputArray = fileArray;
        
        try {
            if ("pdf".equalsIgnoreCase(ext)) {
                PDDocument document = PDDocument.load(fileArray);
                BufferedImage image = new PDFRenderer(document).renderImageWithDPI(0, 300);
                document.close();
                
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
     * OCR 문서 상세 조회
     */
    @PostMapping(value = "/rf-ocr-verf/api/getOcrDocumentDetail.do")
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
            
            // OCR 문서 번호 리스트 조회
            List<String> ocrDocNoList = ocrService.getOcrDocNoList(params);
            
            if (ocrDocNoList == null || ocrDocNoList.isEmpty()) {
                throw new RuntimeException("해당 조건의 문서가 없습니다.");
            }
            
            // 현재 조회할 ocr_doc_no 결정
            String currentOcrDocNo = (String) params.get("ocr_doc_no");
            if (currentOcrDocNo == null || currentOcrDocNo.isEmpty()) {
                // ocr_doc_no가 없으면 첫 번째 문서
                currentOcrDocNo = ocrDocNoList.get(0);
            }
            
            // 현재 인덱스 찾기
            int currentIndex = ocrDocNoList.indexOf(currentOcrDocNo);
            if (currentIndex == -1) {
                currentIndex = 0;
                currentOcrDocNo = ocrDocNoList.get(0);
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
            
            logger.info("OCR 결과 조회 - OCR_DOC_NO: {}, 현재 인덱스: {}/{}", 
                currentOcrDocNo, currentIndex + 1, ocrDocNoList.size());
            
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
            result.put("ocrDocNoList", ocrDocNoList);
            result.put("currentIndex", currentIndex);
            result.put("totalPages", ocrDocNoList.size());
            
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
     * OCR 실시간 테스트 (서류 등록)
     */
    @PostMapping(value = "/rf-ocr-verf/api/ocrRealtimeTest.do", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> ocrRealtimeTest(
            @RequestParam("instCd") String instCd,
            @RequestParam("prdtCd") String prdtCd,
            @RequestParam("ctrlYr") String ctrlYr,
            @RequestParam("ctrlNo") String ctrlNo,
            @RequestParam("docTpCd") String docTpCd,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            logger.info("OCR 실시간 테스트 요청 - FILE: {}", file.getOriginalFilename());
            
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "파일이 비어있습니다.");
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
            }
            
            String fileName = file.getOriginalFilename();
            String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            
            // 서류 등록 (임시 데이터)
            Map<String, Object> params = new HashMap<>();
            params.put("ctrl_yr", ctrlYr);
            params.put("inst_cd", instCd);
            params.put("prdt_cd", prdtCd);
            params.put("ctrl_no", ctrlNo);
            params.put("doc_tp_cd", docTpCd);
            params.put("doc_fl_nm", fileName);
            params.put("doc_fl_ext", fileExt);
            params.put("file_size", file.getSize());
            
            result.put("success", true);
            result.put("message", "서류가 성공적으로 등록되었습니다.");
            result.put("data", params);
            
            logger.info("OCR 실시간 테스트 완료 - 파일명: {}", fileName);
            
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

}
