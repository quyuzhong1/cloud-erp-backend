package com.erp.model.tms.dto;

import com.common.business.enums.UnitEnum;
import com.common.core.enums.CurrencyEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 物流渠道规则约束请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-02-29
*/
@Data
@NoArgsConstructor
public class LogisticsChannelConstraintDTO implements Serializable {




    /**
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO  {

        /**
         * id
         */
        private String id;

        /**
         * 国家二字码
         */
        private String country;
        /**
         * 国家名称
         */
        private String countryName;
        /**
         * 最高报关金额
         */
        private BigDecimal maxCustomsAmount;
        /**
         * 最高报关币别
         */
        private String maxCustomsCurrency;
        /**
         * 最低报关金额
         */
        private BigDecimal minCustomsAmount;
        /**
         * 最低报关币别
         */
        private String minCustomsCurrency;
        /**
         * 重量上限
         */
        private BigDecimal maxWeight;
        /**
         * 重量单位
         */
        private String weightUnit;
        /**
         * 长度上限
         */
        private BigDecimal maxLength;
        /**
         * 宽度上限
         */
        private BigDecimal maxWidth;
        /**
         * 高度上限
         */
        private BigDecimal maxHeight;
        /**
         * 尺寸单位
         */
        private String sizeUnit;
    }


    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 渠道id
        */
        private String channelId;

        /**
        * 国家二字码
        */
        private String country;

        /**
        * 国家名称
        */
        private String countryName;

        /**
        * 最高报关金额
        */
        private BigDecimal maxCustomsAmount;

        /**
        * 最高报关币别
        */
        private String maxCustomsCurrency;

        /**
        * 最低报关金额
        */
        private BigDecimal minCustomsAmount;

        /**
        * 最低报关币种
        */
        private String minCustomsCurrency;

        /**
        * 重量上限
        */
        private BigDecimal maxWeight;

        /**
        * 重量单位
        */
        private String weightUnit;

        /**
        * 长度上限
        */
        private BigDecimal maxLength;

        /**
        * 宽度上限
        */
        private BigDecimal maxWidth;

        /**
        * 高度上限
        */
        private BigDecimal maxHeight;

        /**
        * 尺寸单位
        */
        private String sizeUnit;


    }

    @Data
    @NoArgsConstructor
    public static class AddOrUpdateDTO {

        /**
         * 渠道id
         */
        @NotBlank(message = "渠道id不能为空")
        private String channelId;

        /**
         * 列表
         */
        private List<CommonDTO> commonDTOList;
    }
    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 主键id
         */
        private String id;

        /**
        * 国家二字码
        */
        @NotBlank(message = "国家二字码不能为空")
        private String country;

        /**
        * 国家名称
        */
        @NotBlank(message = "国家名称不能为空")
        private String countryName;

        /**
        * 最高报关金额
        */
        private BigDecimal maxCustomsAmount = BigDecimal.ZERO;

        /**
        * 最高报关币别
        */
        private String maxCustomsCurrency = CurrencyEnum.USD.getCurrencyCode();

        /**
        * 最低报关金额
        */
        private BigDecimal minCustomsAmount = BigDecimal.ZERO;

        /**
        * 最低报关币种
        */
        private String minCustomsCurrency = CurrencyEnum.USD.getCurrencyCode();;

        /**
        * 重量上限
        */
        private BigDecimal maxWeight = BigDecimal.ZERO;;

        /**
        * 重量单位
        */
        private String weightUnit = UnitEnum.WeightUnitEnum.G.getCode();

        /**
        * 长度上限
        */
        private BigDecimal maxLength = BigDecimal.ZERO;

        /**
        * 宽度上限
        */
        private BigDecimal maxWidth = BigDecimal.ZERO;

        /**
        * 高度上限
        */
        private BigDecimal maxHeight = BigDecimal.ZERO;

        /**
        * 尺寸单位
        */
        private String sizeUnit = "cm";


    }


}