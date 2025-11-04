package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Will
 * @version 1.0

 * @date 2023/4/13 10:25
 */
@Data
@NoArgsConstructor
public class PoInstockDetailDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 实收数量
         */
        @NotNull(message = "实收数量不能为空")
        @Min(value = 1,message = "实收数量最小值为1")
        @Max(value = 999999999,message = "实收数量最大值为999999999")
        private Integer stockInQty;

        /**
         * 超收数量
         */
        private Integer exceedQty;

        /**
         * 库位
         */
        private String warehouseLocation;

        /**
         * 备注
         */
        @Size(max = 255,message = "备注不能大于255字符")
        private String remark;

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 采购明细id
         */
        private String purchaseOrderDetailId;

        /**
         * 是否校验（前端无需传值）
         */
        private Boolean isNotCheck;

        /**
         * sku编号（PDA用）
         */
        private String skuNo;
        /**
         * 产品单位
         */
        @NotBlank(message = "单位不能为空")
        private String unitName;
        /**
         * 新品首批
         */
        private String firstMassProduct;

        /**
         * 新品首批
         */
        private String firstMassProductName;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends  AddDTO{
        /**
         * 主键id
         */
        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class ViewDTO extends  UpdateDTO{

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 变体信息
         */
        private String variantProperty;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 规格型号
         */
        private String spuNo;

        /**
         * 单位
         */
        private String unitName;

        /**
         * 采购数量
         */
        private Integer purchaseQty;

        /**
         * 收货数量
         */
        private Integer receiveQty;

        /**
         * 有效入库数量
         */
        private Integer effectiveStockInQty;

        /**
         * 未入库数量
         */
       private Integer unStockInQty;

        /**
         * 仓位
         */
        private String warehouseLocation;

        /**
         * 仓位名称
         */
        private String warehouseLocationName;

        /**
         * 库存状态
         */
        private String inventoryStatusName;

        /**
         * 单价=含税单价/（1+税率）
         */
        private BigDecimal price;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 金额=未税价格*实收数量
         */
        private BigDecimal amount;

        /**
         * 价税合计=含税单价*实收数量
         */
        private BigDecimal taxAmount;

        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 税率
         */
        private String taxRateStr;

        /**
         * 币别
         */
        private String currency;

        /**
         * 新品首批
         */
        private String firstMassProduct;

        /**
         * 新品首批
         */
        private String firstMassProductName;

        /**
         * 币别符号
         */
        private String currencySymbol;
    }
}
