package com.sdk.third.tf.dto;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 通用API响应DTO
 * 用于统一处理TF Fiscal API的响应格式
 * 注意：success字段可能是Boolean类型（true/false）或Integer类型（0表示成功，非0表示失败）
 * 
 * @author system
 * @date 2025/01/XX
 */
@Data
@NoArgsConstructor
public class ApiResponseDTO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * success字段可能是Boolean或Integer类型
     * Boolean: true表示成功，false表示失败
     * Integer: 0表示成功，非0表示失败
     */
    @Alias("success")
    private Object success;
    
    @Alias("message")
    private String message;
    
    @Alias("data")
    private T data;
    
    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        if (success == null) {
            return false;
        }
        if (success instanceof Boolean) {
            return (Boolean) success;
        }
        if (success instanceof Integer) {
            return ((Integer) success) == 0;
        }
        if (success instanceof Number) {
            return ((Number) success).intValue() == 0;
        }
        return false;
    }
}
