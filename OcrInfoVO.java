package com.refine.ocr.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class OcrInfoVO implements Serializable {
    
    @Getter
    private String row_number;
    
    @Getter
    private String ctrl_yr;
    
    @Getter
    private String inst_cd;
    
    @Getter
    private String prdt_cd;
    
    @Getter
    private String ctrl_no;
    
    @Getter
    @Setter
    private String ctrl_key;
    
    @Getter
    private String doc_tp_cd;
    
    @Getter
    private String ocr_doc_no;
    
    @Getter
    @Setter
    private String doc_title;
    
    @Getter
    @Setter
    private String doc_kr_nm;
    
    @Getter
    private String sub_title;
    
    @Getter
    private String ins_dttm;
    
    @Getter
    private String doc_page;
    
    @Getter
    private String ocr_yn;
    
    @Getter
    private String save_yn;
    
    @Getter
    private String verf_yn;
    
    @Getter
    @Setter
    private String prcs_stat;
    
    @Getter
    private String doc_num;
    
    @Getter
    private String item_cd;
    
    @Getter
    private String item_nm;
    
    @Getter
    private String item_value;
    
    @Getter
    private String doc_fl_sav_pth_nm;
    
    @Getter
    private String doc_fl_ext;
    
    @Getter
    private String ocr_rslt_no;
    
    @Getter
    @Setter
    private String doc_fl_nm;
    
    @Getter
    @Setter
    private String enc_yn;
    
    @Getter
    @Setter
    private String cnts;
    
    @Getter
    @Setter
    private String ocr_cnts_original;
    
    @Getter
    @Setter
    private String doc_fl_no;
    
    @Getter
    @Setter
    private String upd_dttm;
    
    @Getter
    @Setter
    private String verf_id;
    
    @Getter
    @Setter
    private String verf_dttm;
    
    @Getter
    @Setter
    private String use_yn;
    
    @Getter
    @Setter
    private String doc_fl_ext_no;
    
    @Getter
    @Setter
    private String eng_title;
    
    @Getter
    @Setter
    private String save_id;
    
    @Getter
    @Setter
    private String save_dttm;
    
    @Getter
    @Setter
    private String replaced;
    
    public void setIns_dttm(String ins_dttm) {
        this.ins_dttm = ins_dttm;
    }
    
    public String toList() {
        return "[" +
                ctrl_yr + ',' +
                inst_cd + ',' +
                prdt_cd + ',' +
                ctrl_no + ',' +
                doc_tp_cd + ',' +
                doc_title + ',' +
                ins_dttm + ',' +
                doc_page + ',' +
                ocr_yn + ',' +
                save_yn + ',' +
                verf_yn + ',' +
                prcs_stat + ',' +
                doc_num + ',' +
                ']';
    }
    
    @Override
    public String toString() {
        return "OcrInfoVO{" +
                "ctrl_yr='" + ctrl_yr + '\'' +
                "inst_cd='" + inst_cd + '\'' +
                "prdt_cd='" + prdt_cd + '\'' +
                "ctrl_no='" + ctrl_no + '\'' +
                "doc_tp_cd='" + doc_tp_cd + '\'' +
                "doc_title='" + doc_title + '\'' +
                "ins_dttm='" + ins_dttm + '\'' +
                "doc_page='" + doc_page + '\'' +
                "ocr_yn='" + ocr_yn + '\'' +
                "save_yn='" + save_yn + '\'' +
                "verf_yn='" + verf_yn + '\'' +
                "prcs_stat='" + prcs_stat + '\'' +
                "doc_num='" + doc_num + '\'' +
                '}';
    }
}
