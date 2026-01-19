package com.sdk.third.tf.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建公司响应DTO
 * 
 * @author system
 * @date 2025/01/XX
 */
public class CreateCompanyResponseDTO {

    /**
     * 创建公司响应数据DTO（data部分）
     */
    @Data
    public static class CreateCompanyDataDTO implements Serializable {
        /**
         * 公司ID
         */
        @JsonProperty("company_id")
        private String companyId;

        /**
         * 公司Token
         */
        @JsonProperty("token")
        private String token;
    }
}
