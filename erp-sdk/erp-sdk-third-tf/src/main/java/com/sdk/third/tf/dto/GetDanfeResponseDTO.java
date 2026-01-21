package com.sdk.third.tf.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 获取发票Danfe响应DTO
 * 
 * @author system
 * @date 2025/01/XX
 */
public class GetDanfeResponseDTO {

    /**
     * Danfe响应数据DTO（data部分）
     */
    @Data
    public static class GetDanfeDataDTO implements Serializable {
        /**
         * Danfe URL链接（PDF文件）
         */
        @JsonProperty("danfe")
        private String danfe;

        /**
         * Danfe简化版URL链接（PDF文件）
         */
        @JsonProperty("danfe_simples")
        private String danfeSimples;
    }
}
