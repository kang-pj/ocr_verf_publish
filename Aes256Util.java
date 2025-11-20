package com.refine.common.component;

import com.refine.config.ConfigProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class Aes256Util {

    private final ConfigProperties configProperties;

    private String iv;

    private String key;

    private final String toss_iv;
    private final String toss_key;
    private final String shinhan_iv;
    private final String shinhan_key;


    public Aes256Util(ConfigProperties configProperties) {
        this.configProperties = configProperties;
        this.toss_iv  = configProperties.getToss_iv();
        this.toss_key  = configProperties.getToss_key();
        this.shinhan_iv  = configProperties.getShinhan_iv();
        this.shinhan_key  = configProperties.getShinhan_key();
    }

    public String Enc_module_by_inst_cd(String inst_cd, String text, String tp){

        switch (inst_cd) {
            case "01":
                iv = shinhan_iv;
                key = shinhan_key;
                break;
            case "49":
                iv = toss_iv;
                key = toss_key;
                break;
            default:
                iv = toss_iv;
                key = toss_key;
        }

        if (tp.equals("E")){
            return encrypt(text);
        }else {
            return decrypt(text);
        }

    }

    public byte[] Enc_module_by_inst_cd_binary(String inst_cd, byte[] imgsrc, String tp) throws Exception {

        switch (inst_cd) {
            case "01":
                iv = shinhan_iv;
                key = shinhan_key;
                break;
            case "49":
                iv = toss_iv;
                key = toss_key;
                break;
            default:
                iv = toss_iv;
                key = toss_key;
        }

        if (tp.equals("E")){
            return encrypt(imgsrc);
        }else {
            return decrypt(imgsrc);
        }

    }



    public String encrypt(String text) {
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(UTF_8));
            SecretKey secretKey = new SecretKeySpec(key.getBytes(UTF_8), "AES");

            Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
            ci.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            return new String(Base64.getEncoder().encode(ci.doFinal(text.getBytes(UTF_8))), UTF_8);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String decrypt(String text) {
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(UTF_8));
            SecretKey secretKey = new SecretKeySpec(key.getBytes(UTF_8), "AES");

            Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
            ci.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            return new String(ci.doFinal(Base64.getDecoder().decode(text.getBytes(UTF_8))), UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] encrypt(byte[] fileData) throws Exception {
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
        SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encrypted = cipher.doFinal(fileData);
        return encrypted;
    }

    public byte[] decrypt(byte[] fileData) throws Exception {

        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(UTF_8));
        SecretKey secretKey = new SecretKeySpec(key.getBytes(UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        return cipher.doFinal(fileData);

    }

}
