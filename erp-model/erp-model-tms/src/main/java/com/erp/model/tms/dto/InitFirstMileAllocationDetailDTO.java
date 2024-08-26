package com.erp.model.tms.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 期初头程分摊明细请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2024-08-13
*/
@Data
@NoArgsConstructor
public class InitFirstMileAllocationDetailDTO implements Serializable {




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
        * 物流单id
        */
        private String mainId;

        /**
        * 发货单id
        */
        private String sourceId;

        /**
        * 发货单明细id
        */
        private String sourceDetailId;

        /**
        * 发货单编号
        */
        private String sourceCode;

        /**
        * 数据来源类型：delivery=发货单
        */
        private String sourceType;

        /**
        * 业务单号
        */
        private String businessCode;

        /**
        * 业务来源类型：FBA=FBA，第三方仓=thirdWarehouse
        */
        private String businessType;

        /**
        * 店铺id
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 仓库ID
        */
        private String warehouseId;

        /**
        * 仓库名称
        */
        private String warehouseName;

        /**
        * sku编码
        */
        private String skuNo;

        /**
        * skuId
        */
        private String skuId;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 上线前签收数量
        */
        private Integer initReceiveQty;

        /**
        * 期初在途头程费用
        */
        private BigDecimal initTransitCost;
        /**
         * 期初在途头程关税
         */
        private BigDecimal initTransitTariff;
        /**
         * 期初暂估头程费用
         */
        private BigDecimal initEstimatedCost;

        /**
        * 期初暂估头程关税
        */
        private BigDecimal initEstimatedTariff;

        /**
        * 分摊重量
        */
        private BigDecimal weightAllocation;

        /**
        * 产品成本
        */
        private String productCost;

        /**
        * 汇率
        */
        private BigDecimal exchangeRate;

        /**
        * 重量单位
        */
        private String weightUnit;

        /**
        * 币种
        */
        private String currency;

        /**
        * 币别符号
        */
        private String currencySymbol;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 主表id
        */

        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 发货单id
        */
        @NotBlank(message = "发货单id不能为空")
        @Size(max = 19,message = "发货单id最大长度不能超过19位")
        private String sourceId;

        /**
        * 发货单明细id
        */
        @NotBlank(message = "发货单明细id不能为空")
        @Size(max = 19,message = "发货单明细id最大长度不能超过19位")
        private String sourceDetailId;

        /**
        * 发货单编号
        */
        @NotBlank(message = "发货单编号不能为空")
        @Size(max = 64,message = "发货单编号最大长度不能超过64位")
        private String sourceCode;

        /**
        * 数据来源类型：delivery=发货单
        */
//        @NotBlank(message = "数据来源类型：delivery=发货单不能为空")
        @Size(max = 30,message = "数据来源类型：delivery=发货单最大长度不能超过30位")
        private String sourceType;

        /**
        * 业务单号
        */
        @NotBlank(message = "业务单号不能为空")
        @Size(max = 64,message = "业务单号最大长度不能超过64位")
        private String businessCode;

        /**
        * 业务来源类型：FBA=FBA，第三方仓=thirdWarehouse
        */
//        @NotBlank(message = "业务来源类型：FBA=FBA，第三方仓=thirdWarehouse不能为空")
        @Size(max = 30,message = "业务来源类型：FBA=FBA，第三方仓=thirdWarehouse最大长度不能超过30位")
        private String businessType;

        /**
        * 店铺id
        */
//        @NotBlank(message = "店铺id不能为空")
        @Size(max = 19,message = "店铺id最大长度不能超过19位")
        private String shopId;

        /**
        * 店铺名称
        */
//        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 64,message = "店铺名称最大长度不能超过64位")
        private String shopName;

        /**
        * 仓库ID
        */
//        @NotBlank(message = "仓库ID不能为空")
        @Size(max = 19,message = "仓库ID最大长度不能超过19位")
        private String warehouseId;

        /**
        * 仓库名称
        */
//        @NotBlank(message = "仓库名称不能为空")
        @Size(max = 64,message = "仓库名称最大长度不能超过64位")
        private String warehouseName;

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;
        /**
         * skuNo
         */
        @NotBlank(message = "skuNo不能为空")
        @Size(max = 19,message = "skuNo最大长度不能超过19位")
        private String skuNo;

        /**
        * 产品名称
        */
//        @NotBlank(message = "产品名称不能为空")
        @Size(max = 500,message = "产品名称最大长度不能超过500位")
        private String productName;

        /**
        * 上线前签收数量
        */
        @NotNull(message = "上线前签收数量不能为空")
        private Integer initReceiveQty;

        /**
        * 期初在途头程费用
        */
        @NotNull(message = "期初在途头程费用不能为空")
        @Digits(integer = 12, fraction = 4, message = "期初在途头程费用整数位不能超过12位，小数位不能超过4位")
        private BigDecimal initTransitCost;
        /**
         * 期初在途头程关税
         */
        @NotNull(message = "期初在途头程关税不能为空")
        @Digits(integer = 12, fraction = 4, message = "期初在途头程关税整数位不能超过12位，小数位不能超过4位")
        private BigDecimal initTransitTariff;
        /**
         * 期初暂估头程费用
         */
        @NotNull(message = "期初暂估头程费用不能为空")
        @Digits(integer = 12, fraction = 4, message = "期初暂估头程费用整数位不能超过12位，小数位不能超过4位")
        private BigDecimal initEstimatedCost;
        /**
        * 期初暂估头程关税
        */
        @NotNull(message = "期初暂估头程关税不能为空")
        @Digits(integer = 12, fraction = 4, message = "期初暂估头程关税整数位不能超过12位，小数位不能超过4位")
        private BigDecimal initEstimatedTariff;

        /**
        * 分摊重量
        */
        @NotNull(message = "分摊重量不能为空")
        @Digits(integer = 12, fraction = 4, message = "分摊重量整数位不能超过12位，小数位不能超过4位")
        private BigDecimal weightAllocation;

        /**
        * 产品成本
        */
        @NotNull(message = "产品成本不能为空")
        @Digits(integer = 12, fraction = 4, message = "产品成本整数位不能超过12位，小数位不能超过4位")
        private String productCost;

        /**
        * 汇率
        */
//        @NotNull(message = "汇率不能为空")
        @Digits(integer = 12, fraction = 4, message = "汇率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal exchangeRate;

        /**
        * 重量单位
        */
//        @NotBlank(message = "重量单位不能为空")
        @Size(max = 30,message = "重量单位最大长度不能超过30位")
        private String weightUnit;

        /**
        * 币种
        */
//        @NotBlank(message = "币种不能为空")
        @Size(max = 30,message = "币种最大长度不能超过30位")
        private String currency;

        /**
        * 币别符号
        */
//        @NotBlank(message = "币别符号不能为空")
        @Size(max = 20,message = "币别符号最大长度不能超过20位")
        private String currencySymbol;


    }


}