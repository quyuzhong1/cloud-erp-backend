package com.sdk.third.tf.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 查询发票详情响应DTO
 * 
 * @author system
 * @date 2025/01/XX
 */
public class InvoiceDetailResponseDTO {

    /**
     * 查询发票详情响应数据DTO（data部分）
     */
    @Data
    public static class InvoiceDetailDataDTO implements Serializable {
        /**
         * ID
         */
        @JsonProperty("id")
        private String id;

        /**
         * UUID
         */
        @JsonProperty("uuid")
        private String uuid;

        /**
         * 发票密钥（chave）
         */
        @JsonProperty("chave")
        private String chave;

        /**
         * 序列号
         */
        @JsonProperty("serie")
        private Integer serie;

        /**
         * 发票编号
         */
        @JsonProperty("number")
        private Integer number;

        /**
         * 状态（Success、Processing、Failed、Canceled、Voided等）
         */
        @JsonProperty("status")
        private String status;

        /**
         * 总价
         */
        @JsonProperty("total_price")
        @JsonAlias({"total_price", "totalPrice"})
        private java.math.BigDecimal totalPrice;

        /**
         * XML文件链接
         */
        @JsonProperty("xml")
        @JsonAlias({"xml", "Xml"})
        private String xml;

        /**
         * 创建时间
         */
        @JsonProperty("create")
        @JsonAlias({"create", "Create"})
        private String create;

        /**
         * 开票时间
         */
        @JsonProperty("issue_time")
        @JsonAlias({"issue_time", "issueTime"})
        private String issueTime;
    }
}
