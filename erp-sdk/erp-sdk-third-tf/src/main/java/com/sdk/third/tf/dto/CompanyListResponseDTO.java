package com.sdk.third.tf.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 公司列表响应DTO
 * 
 * @author system
 * @date 2025/01/XX
 */
public class CompanyListResponseDTO {

    /**
     * 公司列表数据DTO（data部分）
     */
    @Data
    public static class CompanyListDataDTO implements Serializable {
        /**
         * 公司列表
         */
        @JsonProperty("companys")
        private List<CompanyInfoDTO> companys;

        /**
         * 当前页码
         */
        @JsonProperty("page")
        private Integer page;

        /**
         * 总数
         */
        @JsonProperty("total")
        private Integer total;

        /**
         * 总页数
         */
        @JsonProperty("total_pages")
        private Integer totalPages;
    }

    /**
     * 公司信息DTO（列表项）
     */
    @Data
    public static class CompanyInfoDTO implements Serializable {
        /**
         * 公司ID
         */
        @JsonProperty("company_id")
        private Integer companyId;

        /**
         * 税收类型（税务类型）
         */
        @JsonProperty("invoice_type")
        private String invoiceType;

        /**
         * 公司Token
         */
        @JsonProperty("token")
        private String token;

        /**
         * CNPJ
         */
        @JsonProperty("cnpj")
        private String cnpj;

        /**
         * 公司名称
         */
        @JsonProperty("name")
        private String name;

        /**
         * 注册号（州税号）
         */
        @JsonProperty("ie")
        private String ie;

        /**
         * 公司单位类型（主公司或子公司）
         */
        @JsonProperty("unit")
        private String unit;

        /**
         * 公司邮箱
         */
        @JsonProperty("email")
        private String email;

        /**
         * 公司邮编
         */
        @JsonProperty("cep")
        private String cep;

        /**
         * 公司详细地址
         */
        @JsonProperty("address")
        private String address;

        /**
         * 门牌号
         */
        @JsonProperty("house_number")
        private String houseNumber;

        /**
         * 区镇（Bairro）
         */
        @JsonProperty("town")
        private String town;

        /**
         * 城市
         */
        @JsonProperty("city")
        private String city;

        /**
         * 州
         */
        @JsonProperty("state")
        private String state;

        /**
         * A1证书文件地址
         */
        @JsonProperty("cert_file")
        private String certFile;

        /**
         * A1证书密码
         */
        @JsonProperty("cert_pwd")
        private String certPwd;

        /**
         * 发票序列号
         */
        @JsonProperty("serie")
        private Integer serie;

        /**
         * 发票起始编号
         */
        @JsonProperty("number")
        private Integer number;
    }
}
