package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ProductCustomsDTO {
    /**
     * id
     */
    private String id;

    /**
     * skuId
     */
    private String skuId;

    /**
     * skuNo
     */
    private String skuNo;

    /**
     * 国家
     */
    private String country;

    /**
     * 国家
     */
    private String countryName;

    /**
     * 海关编码
     */
    @Size(max = 255, message = "海关编码最大255字符")
    private String customsCode;

    /**
     * 税率
     */
    @Digits(integer = 16,fraction = 2,message = "税率最大16字符，小数位不能大于2个字符")
    private BigDecimal taxRate;

    /**
     * 海关类型：出关 exitCustoms 清关 clearanceCustoms 默认：清关
     * 获取地址：plm/common/enumDropDown?type=CustomsType
     */
    private String type;


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO{
        /**
         * id
         */
        private String id;

        /**
         * skuId
         */
        private String skuId;

        /**
         * skuNo
         */
        private String skuNo;

        /**
         * 国家
         */
        private String country;

        /**
         * 国家
         */
        private String countryName;

        /**
         * 海关编码
         */
        private String customsCode;

        /**
         * 税率
         */
        private BigDecimal taxRate;


    }

}
