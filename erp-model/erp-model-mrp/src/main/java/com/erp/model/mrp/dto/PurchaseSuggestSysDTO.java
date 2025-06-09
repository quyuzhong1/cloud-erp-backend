package com.erp.model.mrp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * <p>
 * 建议采购变更请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-10-21
*/
@Data
@NoArgsConstructor
public class PurchaseSuggestSysDTO implements Serializable {




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
        * 建议采购量
        */
        private Integer suggestPurchaseQty;

        /**
        * 建议采购日期
        */
        private LocalDate suggestPurchaseDate;

        /**
        * 物流方式
        */
        private String logisticsMethod;

        /**
        * 物流时效（天）
        */
        private Integer logisticsDays;

        /**
        * 预计入库日期
        */
        private LocalDate estimateInstockDate;

        /**
        * 预计可售日期
        */
        private LocalDate estimateSalesDate;

        /**
        * 采购成本
        */
        private BigDecimal purchaseCost;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 来源类型
        */
        private String sourceType;


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 建议采购量
        */
        @NotNull(message = "建议采购量不能为空")
        private Integer suggestPurchaseQty;

        /**
        * 建议采购日期
        */
        private LocalDate suggestPurchaseDate;

        /**
        * 物流方式
        */
        @NotBlank(message = "物流方式不能为空")
        @Size(max = 64,message = "物流方式最大长度不能超过64位")
        private String logisticsMethod;

        /**
        * 物流时效（天）
        */
        @NotNull(message = "物流时效（天）不能为空")
        private Integer logisticsDays;

        /**
        * 预计入库日期
        */
        private LocalDate estimateInstockDate;

        /**
        * 预计可售日期
        */
        private LocalDate estimateSalesDate;

        /**
        * 采购成本
        */
        @NotNull(message = "采购成本不能为空")
        @Digits(integer = 12, fraction = 4, message = "采购成本整数位不能超过12位，小数位不能超过4位")
        private BigDecimal purchaseCost;

        /**
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 32,message = "来源类型最大长度不能超过32位")
        private String sourceType;


    }


}