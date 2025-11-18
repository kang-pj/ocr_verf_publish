package com.refine.common.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * S3 접근 유틸
 */
@Component
public class S3Util {
    
    private static final Logger logger = LoggerFactory.getLogger(S3Util.class);
    
    private final S3Client s3Client;
    private final String bucketName;
    
    public S3Util(S3Client s3Client, String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }
    
    /**
     * S3에서 파일 데이터 조회
     * 
     * @param key S3 객체 키
     * @return 파일 바이트 배열
     */
    public byte[] getObject(String key) {
        try {
            logger.debug("S3에서 객체 조회: bucket={}, key={}", bucketName, key);
            
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getObjectRequest);
            byte[] data = toByteArray(response);
            
            logger.debug("S3 객체 조회 완료: key={}, size={} bytes", key, data.length);
            
            return data;
            
        } catch (Exception e) {
            logger.error("S3 객체 조회 실패: key={}", key, e);
            return null;
        }
    }
    
    /**
     * InputStream을 바이트 배열로 변환
     */
    private byte[] toByteArray(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             InputStream stream = inputStream) {
            
            byte[] buffer = new byte[1024];
            int length;
            
            while ((length = stream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            
            return byteArrayOutputStream.toByteArray();
        }
    }
}
