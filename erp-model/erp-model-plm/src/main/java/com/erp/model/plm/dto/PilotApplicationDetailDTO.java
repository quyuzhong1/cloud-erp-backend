package com.erp.model.plm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 试产/量产 明细请求响应实体
 * </p>
 *
 * @author tmj
 * @since 2024-08-27
*/
@Data
@NoArgsConstructor
public class PilotApplicationDetailDTO implements Serializable {

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

        private String mainId;

        /**
        * 单据类型：试产/量产
        */
        private String type;

        private String typeName;

        private String skuId;

        private String skuNo;

        /**
        * 申请数量
        */
        private Integer applyQty;

        /**
        * 批准数量
        */
        private Integer approveQty;

        /**
        * 业务类型
        */
        private String businessType;

        /**
        * 一级供应商ID
        */
        private String mainSupplierId;

        /**
         * 一级供应商名称
         */
        private String mainSupplierName;

        /**
        * 二级供应商ID
        */
        private String secondSupplierId;

        /**
         * 二级供应商名称
         */
        private String secondSupplierName;

        /**
         * 目标含税成本
         */
        private BigDecimal targetTaxCost = BigDecimal.ZERO;
        /**
         * 目标不含税成本
         */
        private BigDecimal targetNoTaxCost = BigDecimal.ZERO;
        /**
         * 实际含税成本
         */
        private BigDecimal actualTaxCost = BigDecimal.ZERO;
        /**
         * 实际不含税成本
         */
        private BigDecimal actualNoTaxCost = BigDecimal.ZERO;

        /**
        * 期望到货日期
        */
        private LocalDate expectArriveDate;

        /**
        * 明细备注
        */
        private String remark;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * sku审核状态
         */
        private int status;
        /**
         * sku审核状态名称
         */
        private String statusName;
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

        private String id;

        private String mainId;

        /**
        * 单据类型：试产/量产
        */
        @NotBlank(message = "下单类型不能为空")
        private String type;

        private String skuId;

        @NotBlank(message = "SKU不能为空")
        private String skuNo;

        /**
        * 申请数量
        */
        @NotBlank(message = "申请数量不能为空")
        private Integer applyQty;

        /**
        * 批准数量
        */
        private Integer approveQty;

        /**
        * 业务类型
        */
        private String businessType;

        /**
        * 一级供应商ID
        */
        @NotBlank(message = "一级供应商不能为空")
        private String mainSupplierId;

        /**
        * 二级供应商ID
        */
        private String secondSupplierId;

        /**
        * 期望到货日期
        */
        private LocalDate expectArriveDate;

        /**
        * 明细备注
        */
        private String remark;
        /**
         * 实际含税单价
         */
        private BigDecimal actualTaxCost;
    }
}