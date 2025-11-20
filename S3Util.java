package com.refine.common.component;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.refine.config.ConfigProperties;
import com.refine.s3.AwsS3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * S3 접근 유틸 (기관별 버킷 관리)
 */
@Component
public class S3Util {
    
    private static final Logger logger = LoggerFactory.getLogger(S3Util.class);
    
    private final S3Client s3Client;
    private final ConfigProperties configProperties;
    private final Aes256Util aes256Util;
    
    private static AmazonS3 kakaoAmazonS3;
    private static AmazonS3 naverAmazonS3;
    private static AmazonS3 amazonS3;
    
    public S3Util(S3Client s3Client, ConfigProperties configProperties, Aes256Util aes256Util) {
        this.s3Client = s3Client;
        this.configProperties = configProperties;
        this.aes256Util = aes256Util;
    }
    
    /**
     * 기관별 이미지 조회
     */
    public byte[] getObject(String instCd, String imagePath) {
        try {
            logger.debug("이미지 조회: instCd={}, imagePath={}", instCd, imagePath);
            
            switch (instCd) {
                case "49":  // 토스
                    return getTossImage(imagePath);
                case "01":  // 신한
                    return getShinhanImage(imagePath);
                case "47":  // 네이버
                    return getNaverImage(imagePath);
                case "45":  // 카카오
                    return getKakaoImage(imagePath);
                default:
                    return getDefaultImage(imagePath);
            }
        } catch (Exception e) {
            logger.error("이미지 조회 실패: instCd={}, imagePath={}", instCd, imagePath, e);
            return null;
        }
    }
    
    /**
     * 토스 이미지 조회
     */
    private byte[] getTossImage(String imagePath) throws Exception {
        String bucket = configProperties.getS3().getBucket();
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(imagePath)
                .build();
        
        ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getObjectRequest);
        return toByteArray(response);
    }
    
    /**
     * 신한 이미지 조회
     */
    private byte[] getShinhanImage(String imagePath) throws Exception {
        String bucket = configProperties.getS3().getBucket();
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(imagePath)
                .build();
        
        ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getObjectRequest);
        return toByteArray(response);
    }
    
    /**
     * 네이버 이미지 조회
     */
    private byte[] getNaverImage(String imagePath) throws Exception {
        ByteBuffer flCnts = null;
        File fl = File.createTempFile("Temp", "");
        try {
            if (naverAmazonS3 == null) {
                String accessKey = configProperties.getS3().getAccessKey() != null ? 
                    configProperties.getS3().getAccessKey() : configProperties.getCredentials().getAccessKey();
                String secretKey = configProperties.getS3().getSecretKey() != null ? 
                    configProperties.getS3().getSecretKey() : configProperties.getCredentials().getSecretKey();
                
                naverAmazonS3 = AmazonS3ClientBuilder.standard()
                        .withCredentials(new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials(accessKey, secretKey)))
                        .withRegion(configProperties.getRegion())
                        .build();
            }
            
            String bucket = configProperties.getS3().getBucket();
            InputStream is = AwsS3.getStream(s3 ->
                    s3.setAmazonS3(naverAmazonS3)
                            .setBucket(bucket)
                            .setAwsS3File(imagePath)
                            .setLocalFile(fl.getPath()));
            
            flCnts = ByteBuffer.allocateDirect(is.available());
            ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
            final ReadableByteChannel src = Channels.newChannel(is);
            
            while (src.read(buffer) != -1) {
                buffer.flip();
                flCnts.put(buffer);
                buffer.compact();
            }
            buffer.flip();
            flCnts.flip();
        } catch (IOException e) {
            logger.error("네이버 이미지 조회 실패", e);
            return null;
        } finally {
            if (fl != null) fl.delete();
        }
        
        byte[] bytes = new byte[flCnts.remaining()];
        flCnts.get(bytes);
        return bytes;
    }
    
    /**
     * 카카오 이미지 조회
     */
    private byte[] getKakaoImage(String imagePath) throws Exception {
        ByteBuffer flCnts = null;
        try {
            if (kakaoAmazonS3 == null) {
                kakaoAmazonS3 = AmazonS3ClientBuilder.standard()
                        .withCredentials(new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials(
                                        configProperties.getKakaoAccessKey(),
                                        configProperties.getKakaoSecretKey())))
                        .withRegion(configProperties.getKakaoRegion())
                        .build();
            }
            
            String bucket = configProperties.getKakaoBucket();
            com.amazonaws.services.s3.model.S3Object s3Object = kakaoAmazonS3.getObject(
                    new com.amazonaws.services.s3.model.GetObjectRequest(bucket, imagePath));
            
            flCnts = ByteBuffer.allocateDirect((int) s3Object.getObjectMetadata().getContentLength());
            ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
            final ReadableByteChannel src = Channels.newChannel(s3Object.getObjectContent());
            
            while (src.read(buffer) != -1) {
                buffer.flip();
                flCnts.put(buffer);
                buffer.compact();
            }
            buffer.flip();
            flCnts.flip();
        } catch (IOException e) {
            logger.error("카카오 이미지 조회 실패", e);
            return null;
        }
        
        byte[] bytes = new byte[flCnts.remaining()];
        flCnts.get(bytes);
        return bytes;
    }
    
    /**
     * 기본 이미지 조회
     */
    private byte[] getDefaultImage(String imagePath) throws Exception {
        ByteBuffer flCnts = null;
        File fl = File.createTempFile("Temp", "");
        try {
            if (amazonS3 == null) {
                String accessKey = configProperties.getS3().getAccessKey() != null ? 
                    configProperties.getS3().getAccessKey() : configProperties.getCredentials().getAccessKey();
                String secretKey = configProperties.getS3().getSecretKey() != null ? 
                    configProperties.getS3().getSecretKey() : configProperties.getCredentials().getSecretKey();
                
                amazonS3 = AmazonS3ClientBuilder.standard()
                        .withCredentials(new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials(accessKey, secretKey)))
                        .withRegion(configProperties.getRegion())
                        .build();
            }
            
            String bucket = configProperties.getS3().getBucket();
            InputStream is = AwsS3.getStream(s3 ->
                    s3.setAmazonS3(amazonS3)
                            .setBucket(bucket)
                            .setAwsS3File(imagePath)
                            .setLocalFile(fl.getPath()));
            
            flCnts = ByteBuffer.allocateDirect(is.available());
            ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
            final ReadableByteChannel src = Channels.newChannel(is);
            
            while (src.read(buffer) != -1) {
                buffer.flip();
                flCnts.put(buffer);
                buffer.compact();
            }
            buffer.flip();
            flCnts.flip();
        } catch (IOException e) {
            logger.error("기본 이미지 조회 실패", e);
            return null;
        } finally {
            if (fl != null) fl.delete();
        }
        
        byte[] bytes = new byte[flCnts.remaining()];
        flCnts.get(bytes);
        return bytes;
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
