package com.refine.hug.service;

import com.refine.hug.model.HugDocFl;
import java.util.Map;

public interface AttachedFileService {
    
    /**
     * 시스템 분류 코드 조회
     */
    String getSysClsCd(Map<String, Object> params);
    
    /**
     * HUG 문서 정보 조회
     */
    HugDocFl getHugDocInfo(Map<String, Object> params);
}
