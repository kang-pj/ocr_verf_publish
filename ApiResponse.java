package com.refine.ocr.vo;

import java.util.HashMap;
import java.util.Map;

/**
 * API 공통 응답 클래스
 */
public class ApiResponse {
    
    private boolean success;
    private String message;
    private Object data;
    private Map<String, Object> extra;
    
    private ApiResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.extra = new HashMap<>();
    }
    
    // 성공 응답
    public static ApiResponse success() {
        return new ApiResponse(true, null, null);
    }
    
    public static ApiResponse success(String message) {
        return new ApiResponse(true, message, null);
    }
    
    public static ApiResponse success(Object data) {
        return new ApiResponse(true, null, data);
    }
    
    public static ApiResponse success(String message, Object data) {
        return new ApiResponse(true, message, data);
    }
    
    // 실패 응답
    public static ApiResponse fail(String message) {
        return new ApiResponse(false, message, null);
    }
    
    public static ApiResponse fail(String message, Object data) {
        return new ApiResponse(false, message, data);
    }
    
    // 추가 데이터
    public ApiResponse put(String key, Object value) {
        this.extra.put(key, value);
        return this;
    }
    
    // Map으로 변환
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        if (message != null) {
            result.put("message", message);
        }
        if (data != null) {
            result.put("data", data);
        }
        if (extra != null && !extra.isEmpty()) {
            result.putAll(extra);
        }
        return result;
    }
    
    // Getters
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Object getData() {
        return data;
    }
    
    public Map<String, Object> getExtra() {
        return extra;
    }
}
