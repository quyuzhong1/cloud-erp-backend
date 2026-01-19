package com.sdk.third.tf.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 退货发票响应DTO
 * 
 * @author system
 * @date 2025/01/XX
 */
public class ReturnInvoiceResponseDTO {

    /**
     * 退货发票响应数据DTO（data部分）
     */
    @Data
    public static class ReturnInvoiceDataDTO implements Serializable {
        /**
         * UUID
         */
        @JsonProperty("uuid")
        private String uuid;

        /**
         * 状态（Success、Failed等）
         */
        @JsonProperty("status")
        private String status;

        /**
         * 原因/说明
         */
        @JsonProperty("motivo")
        private String motivo;

        /**
         * NF-e编号
         */
        @JsonProperty("nfe")
        private Integer nfe;

        /**
         * 序列号
         */
        @JsonProperty("serie")
        private Integer serie;

        /**
         * 发票密钥（chave）
         */
        @JsonProperty("chave")
        private String chave;

        /**
         * 发票模式（nfe/nfce）
         */
        @JsonProperty("modelo")
        private String modelo;

        /**
         * EPEC标识
         */
        @JsonProperty("epec")
        private Boolean epec;

        /**
         * XML内容或XML文件链接
         */
        @JsonProperty("xml")
        private String xml;
    }
}
