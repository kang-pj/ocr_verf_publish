package com.refine.common.service;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.refine.common.component.Aes256Util;
import com.refine.common.exception.InternalServerException;
import com.refine.config.ConfigProperties;
import com.refine.hug.model.FlInf;
import com.refine.common.model.HugPreReqInf;
import com.refine.s3.AwsS3;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;


import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Configuration
public class FileUploadService {
    private final ConfigProperties configProperties;
    private final String bucketName;
    private final String accessKey;
    private final String secretKey;
    private final String kakaoBucket;
    private final String kakaoSecretKey;
    private final String region;
    private final String kakaoAccessKey;
    private final String kakaoRegion;


    private final  Aes256Util aes256Util;
    private final S3Client s3Client;
    private static AmazonS3 kakaoAmazonS3;
    private static AmazonS3 naverAmazonS3;
    private static AmazonS3 amazonS3;
    private  final  RefineBizService refineBizService;

    public FileUploadService(Aes256Util aes256Util, S3Client s3Client, RefineBizService refineBizService, ConfigProperties configProperties) {
        this.aes256Util = aes256Util;
        this.s3Client = s3Client;
        this.refineBizService = refineBizService;
        this.configProperties = configProperties;
        this.bucketName = configProperties.getS3().getBucket();
        this.accessKey = configProperties.getCredentials().getAccessKey();
        this.secretKey = configProperties.getCredentials().getSecretKey();
        this.region = configProperties.getRegion();
        this.kakaoBucket = configProperties.getKakaoBucket();
        this.kakaoAccessKey = configProperties.getKakaoAccessKey();
        this.kakaoSecretKey = configProperties.getKakaoSecretKey();
        this.kakaoRegion = configProperties.getKakaoRegion();
    }

    public void uploadFile(MultipartFile file, String path) {

        try {

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(path)
                    .build();
            s3Client.putObject(objectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        } catch (IOException e) {
            throw new InternalServerException("S3 File Upload Fail : " +e.getMessage(), "errors.application.s3upload_fail");
        }

    }

    public List<String> listFiles() {

        ListObjectsV2Request listObjectsReqManual = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .maxKeys(50)
                .build();

        ListObjectsV2Response listObjResponse = s3Client.listObjectsV2(listObjectsReqManual);
        List<S3Object> objects = listObjResponse.contents();

        return objects.stream().map(S3Object::key).collect(Collectors.toList());
    }

    public List<String> listFilesInFolder(String folderPath) {
        List<String> fileList = new ArrayList<>();

        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(folderPath)
                .build();

        s3Client.listObjectsV2(listObjectsV2Request).contents().forEach(s3Object -> {
            fileList.add(s3Object.key());
        });

        return fileList;
    }

    public List<FlInf> listFilesInF(HugPreReqInf hugPreReqInf , String folderPath) {


        List<FlInf> fileList = new ArrayList<>();

        FlInf flInf = new FlInf();
        flInf.setInstCd(hugPreReqInf.getInstCd());
        flInf.setAppUserId(hugPreReqInf.getClientId());
        flInf.setApId(hugPreReqInf.getClientReqNo());

        List<JSONPObject> jsonpObjectList = new ArrayList<>();

        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(folderPath)
                .build();

        s3Client.listObjectsV2(listObjectsV2Request).contents().forEach(s3Object -> {

            JSONPObject jsonpObject = new JSONPObject( "fl_path", s3Object.key() );

            jsonpObjectList.add(jsonpObject);

        });
        flInf.setFlInf((List<JSONPObject>) jsonpObjectList);


        return fileList;
    }


    public InputStream downloadFile(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        ResponseInputStream<GetObjectResponse> s3ObjectInputStream = s3Client.getObject(getObjectRequest);
        return s3ObjectInputStream;
    }


    public void deleteFile(String filePath) {
        s3Client.deleteObject(builder -> builder.bucket(bucketName).key(filePath));
    }


    public ResponseInputStream<GetObjectResponse> previewImage(String imagePath) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(imagePath)
                .build();

        ResponseInputStream<GetObjectResponse> s3ObjectStream = s3Client.getObject(getObjectRequest);
        return s3ObjectStream;
    }

    public byte[] previewEncImage(String imagePath , String instCd) throws Exception {


        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(imagePath)
                .build();

        ResponseInputStream<GetObjectResponse> s3ObjectStream = s3Client.getObject(getObjectRequest);

        byte[] imgsrc2 = aes256Util.Enc_module_by_inst_cd_binary(instCd, s3ObjectStream.readAllBytes(), "D");
        byte[] decodedData = new String(imgsrc2, StandardCharsets.UTF_8).getBytes(StandardCharsets.ISO_8859_1);
        return  decodedData;


    }

    public byte[] toByteArray(ResponseInputStream<GetObjectResponse> stream) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             InputStream is = stream) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            // 예외 처리
            e.printStackTrace();
            return null;
        }
    }


    public static String readStream(ResponseInputStream<GetObjectResponse> stream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = stream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }

    public static String readStreamAsBase64(ResponseInputStream<GetObjectResponse> stream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = stream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return Base64.getEncoder().encodeToString(result.toByteArray());
    }

    //사용안함
    /*public InputStream decryptReadStream(String instCd, ResponseInputStream<GetObjectResponse> stream) throws Exception {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = stream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        Aes256Util aes256Util = new Aes256Util();
        return new ByteArrayInputStream(aes256Util.Enc_module_by_inst_cd_binary(instCd, result.toByteArray(), "D"));
        //return result.toString(StandardCharsets.UTF_8.name());
    }*/


    public String generatePresignedUrl(String key) {

        S3Presigner presigner = S3Presigner.builder()
                .region(Region.AWS_GLOBAL)
                .build();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket("rf-files")
                .key(key)
                .build();

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedGetObjectRequest = presigner.presignGetObject(getObjectPresignRequest);

        URL url = presignedGetObjectRequest.url();
        System.out.println("Presigned URL: " + url);
        return url.toString();
    }

    public byte[] kakaoPreviewImage(String imagePath) {
        ByteBuffer flCnts = null;
        try {
            if(kakaoAmazonS3 == null) {
                kakaoAmazonS3 = AmazonS3ClientBuilder.standard()
                        .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(this.kakaoAccessKey, this.kakaoSecretKey)))
                        .withRegion(this.kakaoRegion)
                        .build();
            }
            com.amazonaws.services.s3.model.S3Object s3Object = kakaoAmazonS3.getObject(new com.amazonaws.services.s3.model.GetObjectRequest(this.kakaoBucket, imagePath));

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
            e.printStackTrace();
            return null;
        }
        byte[] bytes = new byte[flCnts.remaining()];
        flCnts.get(bytes);
        return bytes;
    }

    public byte[] naverPreviewImage(String imagePath) throws Exception {
        ByteBuffer flCnts = null;
        File fl = File.createTempFile("Temp", "");
        try {
            if(this.naverAmazonS3 == null) {
                this.naverAmazonS3 = AmazonS3ClientBuilder.standard()
                        .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(this.accessKey, this.secretKey)))
                        .withRegion(this.kakaoRegion)
                        .build();
            }
            InputStream is = AwsS3.getStream(s3 ->
                    s3.setAmazonS3(this.naverAmazonS3)
                            .setBucket(bucketName)
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
            e.printStackTrace();
            return null;
        }
        byte[] bytes = new byte[flCnts.remaining()];
        flCnts.get(bytes);
        if (fl != null)
            fl.delete();
        return bytes;
    }

    public byte[] leasePreviewImage(String imagePath) throws Exception {
        ByteBuffer flCnts = null;
        File fl = File.createTempFile("Temp", "");
        try {
            if(this.amazonS3 == null) {
                this.amazonS3 = AmazonS3ClientBuilder.standard()
                        .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(this.accessKey, this.secretKey)))
                        .withRegion(this.region)
                        .build();
            }
            InputStream is = AwsS3.getStream(s3 ->
                    s3.setAmazonS3(this.amazonS3)
                            .setBucket(bucketName)
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
            e.printStackTrace();
            return null;
        }
        byte[] bytes = new byte[flCnts.remaining()];
        flCnts.get(bytes);
        if (fl != null)
            fl.delete();
        return bytes;
    }
}