package com.refine.ocr.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class OcrItemVO implements Serializable {
    
    @Getter
    @Setter
    private String inst_cd;
    
    @Getter
    @Setter
    private String prdt_cd;
    
    @Getter
    @Setter
    private String item_cd;
    
    @Getter
    @Setter
    private String item_nm;
    
    @Getter
    @Setter
    private Integer item_odr;
    
    @Getter
    @Setter
    private String use_yn;
    
    @Getter
    @Setter
    private String insr_id;
    
    @Getter
    @Setter
    private String ins_dttm;
    
    @Getter
    @Setter
    private String updr_id;
    
    @Getter
    @Setter
    private String upd_dttm;
    
    @Override
    public String toString() {
        return "OcrItemVO{" +
                "inst_cd='" + inst_cd + '\'' +
                ", prdt_cd='" + prdt_cd + '\'' +
                ", item_cd='" + item_cd + '\'' +
                ", item_nm='" + item_nm + '\'' +
                ", item_odr=" + item_odr +
                ", use_yn='" + use_yn + '\'' +
                '}';
    }
}
