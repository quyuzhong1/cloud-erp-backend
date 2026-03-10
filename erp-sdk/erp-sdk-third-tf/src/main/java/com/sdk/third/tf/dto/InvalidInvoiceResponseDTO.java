package com.sdk.third.tf.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 作废发票响应DTO
 * 
 * @author system
 * @date 2025/01/XX
 */
public class InvalidInvoiceResponseDTO {

    /**
     * 作废发票响应数据DTO（data部分）
     */
    @Data
    public static class InvalidInvoiceDataDTO implements Serializable {
        /**
         * UUID
         */
        @JsonProperty("uuid")
        private String uuid;

        /**
         * XML文件链接
         */
        @JsonProperty("xml")
        private String xml;
    }
}
