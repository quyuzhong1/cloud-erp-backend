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
 * 头程重量分摊请求响应实体
 * </p>
 *
 * @author tmj
 * @since 2024-08-20
*/
@Data
@NoArgsConstructor
public class FirstMileWeightAllocationDTO implements Serializable {




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
        * 来源ID
        */
        private String sourceId;

        /**
        * 来源单号
        */
        private String sourceCode;

        /**
        * 发货单明细id
        */
        private String deliveryDetailId;

        /**
        * 业务单号
        */
        private String businessCode;

        /**
        * 物流运单号
        */
        private String transportNo;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 费用状态
        */
        private String allocationStatus;

        private String skuId;

        private String skuNo;

        /**
        * 平台skuId
        */
        private String platformSkuId;

        /**
        * 平台skuNo
        */
        private String platformSkuNo;

        /**
        * 箱ID
        */
        private String boxId;

        /**
        * 箱号
        */
        private String boxNo;

        /**
        * 发货量
        */
        private Integer deliveryQty;

        /**
        * 箱长
        */
        private Integer boxLength;

        /**
        * 箱宽
        */
        private Integer boxWide;

        /**
        * 箱高
        */
        private Integer boxHigh;

        /**
        * 箱子尺寸单位
        */
        private String boxSizeUnit;

        /**
        * 出库计费重
        */
        private BigDecimal chargedWeight;

        /**
        * 体积重
        */
        private BigDecimal volumeWeight;

        /**
        * 单产品重量
        */
        private BigDecimal productWeight;

        /**
        * 分摊重量
        */
        private BigDecimal allocationWeight;

        /**
        * 重量单位
        */
        private String weightUnit;

        /**
        * 物流商ID
        */
        private String supplierId;

        /**
        * 物流商名称
        */
        private String supplierName;

        /**
        * 重量分摊方式
        */
        private String allocationType;

        /**
        * 计费规则
        */
        private String billingRule;

        /**
        * 店铺ID
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 目的国家编码
        */
        private String toCountry;

        /**
        * 发货仓库ID
        */
        private String fromWarehouseId;

        /**
        * 核算期间id
        */
        private String calculatePeriodId;

        /**
        * 核算月份
        */
        private String calculateMonth;


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
        * 来源ID
        */
        private String sourceId;

        /**
        * 来源单号
        */
        private String sourceCode;

        /**
        * 发货单明细id
        */
        private String deliveryDetailId;

        /**
        * 业务单号
        */
        private String businessCode;

        /**
        * 物流运单号
        */
        private String transportNo;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 费用状态
        */
        private String allocationStatus;

        private String skuId;

        /**
        * 平台skuId
        */
        private String platformSkuId;

        /**
        * 平台skuNo
        */
        private String platformSkuNo;

        /**
        * 箱ID
        */
        private String boxId;

        /**
        * 箱号
        */
        private String boxNo;

        /**
        * 发货量
        */
        private Integer deliveryQty;

        /**
        * 箱长
        */
        private Integer boxLength;

        /**
        * 箱宽
        */
        private Integer boxWide;

        /**
        * 箱高
        */
        private Integer boxHigh;

        /**
        * 箱子尺寸单位
        */
        private String boxSizeUnit;

        /**
        * 出库计费重
        */
        private BigDecimal chargedWeight;

        /**
        * 体积重
        */
        private BigDecimal volumeWeight;

        /**
        * 单产品重量
        */
        private BigDecimal productWeight;

        /**
        * 分摊重量
        */
        private BigDecimal allocationWeight;

        /**
        * 重量单位
        */
        private String weightUnit;

        /**
        * 物流商ID
        */
        private String supplierId;

        /**
        * 物流商名称
        */
        private String supplierName;

        /**
        * 重量分摊方式
        */
        private String allocationType;

        /**
        * 计费规则
        */
        private String billingRule;

        /**
        * 店铺ID
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 目的国家编码
        */
        private String toCountry;

        /**
        * 发货仓库ID
        */
        private String fromWarehouseId;

        /**
        * 核算期间id
        */
        private String calculatePeriodId;

        /**
        * 核算月份
        */
        private String calculateMonth;


    }


}