package com.erp.model.tms.dto;

import com.common.business.enums.UnitEnum;
import com.common.core.enums.CurrencyEnum;
import com.erp.model.tms.entity.LogisticsChannelConstraintEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

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
        @Valid
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
        private String minCustomsCurrency = CurrencyEnum.USD.getCurrencyCode();

        /**
        * 重量上限
        */
        private BigDecimal maxWeight = BigDecimal.ZERO;

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

        public void setMaxCustomsAmount(BigDecimal maxCustomsAmount) {
            if(maxCustomsAmount == null){
                return;
            }
            this.maxCustomsAmount = maxCustomsAmount;
        }

        public void setMaxCustomsCurrency(String maxCustomsCurrency) {
            if(maxCustomsCurrency == null){
                return;
            }
            this.maxCustomsCurrency = maxCustomsCurrency;
        }

        public void setMinCustomsAmount(BigDecimal minCustomsAmount) {
            if(minCustomsAmount == null){
                return;
            }
            this.minCustomsAmount = minCustomsAmount;
        }

        public void setMinCustomsCurrency(String minCustomsCurrency) {
            if(minCustomsCurrency == null){
                return;
            }
            this.minCustomsCurrency = minCustomsCurrency;
        }

        public void setMaxWeight(BigDecimal maxWeight) {
            if(maxWeight == null){
                return;
            }
            this.maxWeight = maxWeight;
        }

        public void setWeightUnit(String weightUnit) {
            if(weightUnit == null){
                return;
            }
            this.weightUnit = weightUnit;
        }

        public void setMaxLength(BigDecimal maxLength) {
            if(maxLength == null){
                return;
            }
            this.maxLength = maxLength;
        }

        public void setMaxWidth(BigDecimal maxWidth) {
            if(maxWidth == null){
                return;
            }
            this.maxWidth = maxWidth;
        }

        public void setMaxHeight(BigDecimal maxHeight) {
            if(maxHeight == null){
                return;
            }
            this.maxHeight = maxHeight;
        }

        public void setSizeUnit(String sizeUnit) {
            if(sizeUnit == null){
                return;
            }
            this.sizeUnit = sizeUnit;
        }

        public boolean equalsEntity(Object o) {
            if (this == o) return true;
            if (o == null || LogisticsChannelConstraintEntity.class != o.getClass()) return false;
            LogisticsChannelConstraintEntity commonDTO = (LogisticsChannelConstraintEntity) o;
            return Objects.equals(country, commonDTO.getCountry()) && Objects.equals(countryName, commonDTO.getCountryName()) && Objects.equals(maxCustomsAmount, commonDTO.getMaxCustomsAmount()) && Objects.equals(maxCustomsCurrency, commonDTO.getMaxCustomsCurrency()) && Objects.equals(minCustomsAmount, commonDTO.getMinCustomsAmount()) && Objects.equals(minCustomsCurrency, commonDTO.getMinCustomsCurrency()) && Objects.equals(maxWeight, commonDTO.getMaxWeight()) && Objects.equals(weightUnit, commonDTO.getWeightUnit()) && Objects.equals(maxLength, commonDTO.getMaxLength()) && Objects.equals(maxWidth, commonDTO.getMaxWidth()) && Objects.equals(maxHeight, commonDTO.getMaxHeight()) && Objects.equals(sizeUnit, commonDTO.getSizeUnit());
        }

        public boolean isValid() {
            if ( minCustomsAmount.compareTo(BigDecimal.ZERO) == 0 && maxCustomsAmount.compareTo(BigDecimal.ZERO) == 0 &&
                    maxWeight.compareTo(BigDecimal.ZERO) == 0 && maxLength.compareTo(BigDecimal.ZERO) == 0 &&
                    maxWidth.compareTo(BigDecimal.ZERO) == 0 && maxHeight.compareTo(BigDecimal.ZERO) == 0) {
                return false;
            }
            return true;
        }

        public boolean isValidSize() {
            if (maxLength.compareTo(BigDecimal.ZERO) > 0 && maxWidth.compareTo(BigDecimal.ZERO) > 0 && maxHeight.compareTo(BigDecimal.ZERO) > 0) {
                return true;
            }
            if (maxLength.compareTo(BigDecimal.ZERO) > 0 || maxWidth.compareTo(BigDecimal.ZERO) > 0 || maxHeight.compareTo(BigDecimal.ZERO) > 0) {
                return false;
            }
            return true;
        }
    }


}