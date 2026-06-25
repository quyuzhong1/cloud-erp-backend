package com.erp.model.oms.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Address parse request/response DTO.
 */
@Data
@NoArgsConstructor
public class AddressParseDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class ParseRequestDTO implements Serializable {
        /**
         * Raw address text to parse.
         */
        @NotBlank(message = "待解析地址不能为空")
        @Size(max = 1000, message = "待解析地址最大长度不能超过1000位")
        private String fullAddress;
    }

    @Data
    @NoArgsConstructor
    public static class ParseResultDTO implements Serializable {
        private String countryId;
        private String countryName;

        private String provinceId;
        private String province;

        private String cityId;
        private String city;

        private String districtId;
        private String district;

        private String detailAddress;
        private String contactName;
        private String phone;
        private String zipCode;

        private BigDecimal confidence;
        private String sourceText;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class BatchParseRequestDTO extends ParseRequestDTO {

        /**
         * id
         */
        private String id;

    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class BatchParseResultDTO extends ParseResultDTO {

        /**
         * id
         */
        private String id;

    }
}
