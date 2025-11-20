package com.refine.hug.controller;

import com.refine.common.component.Aes256Util;

import com.refine.common.exception.InternalServerException;
import com.refine.common.service.FileUploadService;
import com.refine.common.service.RefineBizService;
import com.refine.hug.model.HugDocFl;
import com.refine.hug.service.AttachedFileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

@RestController
@RequestMapping("/v1/application/file")
public class AttachedFileController {

    protected final RefineBizService refineBizService;
    protected final AttachedFileService attachedFileService;
    protected final FileUploadService fileUploadService;
    protected final Aes256Util aes256Util;
        public AttachedFileController(RefineBizService refineBizService, AttachedFileService attachedFileService, FileUploadService fileUploadService, Aes256Util aes256Util){
        this.refineBizService = refineBizService;
        this.attachedFileService = attachedFileService;
        this.fileUploadService = fileUploadService;
        this.aes256Util = aes256Util;
    }

    //사용안함
    /*@Value("${cloud.aws.s3.bucket}")
    private String bucketName;*/

    Logger logger = Logger.getLogger(this.getClass().getName());

    public byte[] toByteArray(ResponseInputStream<GetObjectResponse> inputStream) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             InputStream stream = inputStream) {  // 자동으로 stream을 닫아주는 try-with-resources 사용

            byte[] buffer = new byte[1024];
            int length;

            while ((length = stream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }

            return byteArrayOutputStream.toByteArray();
        }
    }

    // 네이버,카카오페이,신한,토스 에서 S3 업로드한 파일 이미지 미리보기
    @GetMapping(value="/preview-image-all")
    public ResponseEntity<byte[]> previewImageAll(@RequestParam String instCd, @RequestParam String prdtCd, @RequestParam String imagePath) throws Exception {

        logger.info("## previewImageAll in ##");
        Map<String, Object> mapParams = new HashMap<>();
        mapParams.put("instCd", instCd);
        mapParams.put("prdtCd", prdtCd);
        mapParams.put("imgPath", imagePath);
        String sysClsCd = attachedFileService.getSysClsCd(mapParams);
        byte[] imageData;
        String contentType = MediaType.IMAGE_JPEG_VALUE;

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(imagePath);
            String decodedPath = new String(decodedBytes, StandardCharsets.UTF_8);
            
            // 파일 확장자로 Content-Type 결정
            if (decodedPath.toLowerCase().endsWith(".png")) {
                contentType = MediaType.IMAGE_PNG_VALUE;
            } else if (decodedPath.toLowerCase().endsWith(".gif")) {
                contentType = MediaType.IMAGE_GIF_VALUE;
            } else if (decodedPath.toLowerCase().endsWith(".tif") || decodedPath.toLowerCase().endsWith(".tiff")) {
                contentType = "image/tiff";
            } else {
                contentType = MediaType.IMAGE_JPEG_VALUE;
            }
            
            imagePath = decodedPath;

            if ("KRH".equals(sysClsCd)) { // 반환보증
                if (instCd.equals("49")) { // 토스
                    mapParams.put("doc_att_fl_decoded", imagePath);
                    HugDocFl hugDocFl = attachedFileService.getHugDocInfo(mapParams);

                    if ("Y".equals(hugDocFl.getEncYn())) {
                        imagePath = aes256Util.Enc_module_by_inst_cd(instCd, imagePath, "D");
                        imageData = fileUploadService.previewEncImage(imagePath, instCd);
                    } else {
                        ResponseInputStream<GetObjectResponse> imageStream = fileUploadService.previewImage(imagePath);
                        imageData = toByteArray(imageStream);
                    }
                } else if (instCd.equals("01")) { // 신한
                    ResponseInputStream<GetObjectResponse> fileStream = fileUploadService.previewImage(imagePath);
                    imageData = toByteArray(fileStream);
                } else if (instCd.equals("47")) { // 네이버
                    imageData = fileUploadService.naverPreviewImage(imagePath);
                } else { // 카카오
                    imageData = fileUploadService.kakaoPreviewImage(imagePath);
                }
            } else if ("JL".equals(sysClsCd) || "KH".equals(sysClsCd) || "ITL".equals(sysClsCd) || "M".equals(sysClsCd) || "IM".equals(sysClsCd) || "SCLS".equals(sysClsCd) || "BY".equals(sysClsCd)) { // 전세 , 저당
                imageData = fileUploadService.leasePreviewImage(imagePath);
            } else if(instCd != null && prdtCd != null && instCd.equals("00") && prdtCd.equals("00")){  // Fax 관련 test code 추가
                logger.info("fax 진입 성공");
                imageData = fileUploadService.leasePreviewImage(imagePath);
            } else {
                ResponseInputStream<GetObjectResponse> imageStream = fileUploadService.previewImage(imagePath);
                imageData = toByteArray(imageStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalServerException("This is an internal server error.", "errors.application.read_image_fail");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(imageData);
    }


}
