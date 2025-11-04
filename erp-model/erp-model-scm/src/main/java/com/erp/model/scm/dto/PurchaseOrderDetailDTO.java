package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 采购订单明细DTO
 * @date 2023/3/16 14:43
 */
@Data
@NoArgsConstructor
public class PurchaseOrderDetailDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        @NotBlank(message = "sku编码不能为空")
        private String skuNo;

        /**
         * 是否加急（false否，true是）
         */
        private Boolean isUrgent;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 变体信息
         */
        private String variantProperty;

        /**
         * 报关型号
         */
        private String declareModel;

        /**
         * 报关名称
         */
        private String declareName;

        /**
         * 含税单价
         */
        @Digits(integer = 16,fraction = 4,message = "含税单价最大16字符，小数位不能大于4个字符")
        private BigDecimal taxPrice;

        /**
         * 税率
         */
        @Digits(integer = 16,fraction = 4,message = "税率最大16字符，小数位不能大于4个字符")
        private BigDecimal taxRate;

        /**
         * 币别
         */
        private String currency;

        /**
         * 币别符号
         */
        private String currencySymbol;

        /**
         * 采购数量
         */
        @NotNull(message = "采购数量不能为空")
        @Min(value = 0,message = "采购数量最小值为0")
        @Max(value = 999999999,message = "采购数量最大值为999999999")
        private Integer purchaseQty;

        /**
         * 采购金额
         */
        private BigDecimal purchaseAmount;

        /**
         * 预计交货日期
         */
        @NotNull(message = "预计交货日期不能为空")
        private LocalDate planDeliveryDate;

        /**
         * 采购交期（天）
         */
        private Integer deliveryDay;

        /**
         * 是否是赠品（false否，true是）
         */
        private Boolean isGift;

        /**
         * 新品首批（false否,true是）
         */
        private String firstMassProduct;

        /**
         * 新品首批（false否,true是）名字
         */
        private String firstMassProductName;

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
         * 委外子SKU下推可用
         */
        private String warehouseLocation;

        /**
         * 采购申请明细id(无需传值，后端使用)
         */
        private String purchaseApplicationDetailId;

        /**
         * 采购申请id(无需传值，后端使用)
         */
        private String purchaseApplicationId;

        /**
         * 签收数量
         */
        private Integer receiveQty;

        /**
         * 未交货数量
         */
        private Integer unReceiveQty;
        /**
         * 执行状态 ,PurchaseOrderConfirmTypeEnum枚举
         */
        private String executionStatus;
        /**
         * 执行状态描述
         */
        private String executionStatusName;
        /**
         * 是否需要重新赋值税率
         */
        private Boolean isRevalueTaxRate = true;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends AddDTO{

        /**
         * 主表id
         */
        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class ImportDTO {
        /**
         * 成功返回数据
         */
        private List<PurchaseOrderDetailDTO.AddDTO> successList;

        /**
         * 错误url
         */
        private String errorUrl;
    }

    @Data
    @NoArgsConstructor
    public static class ExportPdfDTO {
        /**
         * 产品图片 主图
         */
        private String image;
        /**
         * 物料编码
         */
        private String skuNo;
        /**
         * spuNo
         */
        private String spuNo;

        /**
         * 名称
         */
        private String declareName;

        /**
         * 型号
         */
        private String declareModel;

        /**
         * 描述
         */
        private String productName;

        /**
         * 数量
         */
        private Integer purchaseQty;

        /**
         * 单位
         */
        private String unitName;

        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 不含税单价
         */
        private BigDecimal price;
        /**
         * 不含税单价 使用科学计数法展示
         */
        private String priceStr;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;
        /**
         * 含税单价 使用科学计数法展示
         */
        private String taxPriceStr;

        /**
         * 不含税金额
         */
        private BigDecimal notTaxPurchaseAmount;
        /**
         * 不含税金额 使用科学计数法展示
         */
        private String notTaxPurchaseAmountStr;

        /**
         * 含税金额
         */
        private BigDecimal purchaseAmount;
        /**
         * 含税金额 使用科学计数法展示
         */
        private String purchaseAmountStr;

        /**
         * 交期
         */
        private LocalDate planDeliveryDate;

        /**
         * 备注
         */
        private String remark;

        /**
         * 是否赠品
         */

        private Boolean isGift;
        private String isGiftStr;
        /**
         * 新品首批
         */
        private String firstMassProduct;
        private String firstMassProductName;
        /**
         * 是否加急
         */
        private Boolean isUrgent;
        private String isUrgentStr;





    }

    @Data
    @NoArgsConstructor
    public static class ProductSearchParamDTO {

        /**
         * 采购订单id
         */
        @NotBlank(message = "采购订单id不能为空")
        private String purchaseOrderId;

        /**
         * sku编号集合
         */
        private List<String>  skuNoList;

        /**
         * 远程搜索sku
         */
        private String remoteSearchSku;

    }

    @Data
    @NoArgsConstructor
    public static class ViewProductDTO {

        /**
         * 采购单号
         */
        private String  code;
        /**
         * 订单类型
         */
        private String  type;
        /**
         * 来源单号id
         */
        private String  sourceId;
        /**
         * 来源单号
         */
        private String  sourceCode;
        /**
         * 来源类型
         */
        private String  sourceType;
        /**
         * 委外类型
         */
        private String  subcontractType;

        /**
         * skuId
         */
        private String  skuId;

        /**
         * sku编码
         */
        private String  skuNo;
        /**
         * 单位名称
         */
        private String unitName;
        /**
         * ean编码
         */
        private String ean;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 预计交货日期
         */
        private LocalDate planDeliveryDate;

        /**
         * 采购数量
         */
        private Integer purchaseQty;

        /**
         * 收货数量
         */
        private Integer receiveQty;

        /**
         * 已发货数量
         */
        private Integer deliveryQty;

        /**
         * 未交货数量
         */
        private Integer unReceiveQty;

        /**
         * 未入库数量
         */
        private Integer unStockInQty;

        /**
         * 有效入库数量
         */
        private Integer effectiveStockInQty;

        /**
         * 已入库数量
         */
        private Integer hasStockInQty;

        /**
         * 实退数量
         */
        private Integer realityReturnQty;

        /**
         * 超收数量(赠品收货数量)
         */
        private Integer exceedQty;

        /**
         * 收料组织名称
         */
        private String  receiveOrgName;

        /**
         * 是否赠品
         */
        private Boolean isGift;

        /**
         * 是否加急
         */
        private Boolean isUrgent;

        /**
         * 备注
         */
        private String remark;

        /**
         * 订单明细id
         */
        private String purchaseOrderDetailId;

        /**
         * 变体信息
         */
        private String variantProperty;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;
        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 币别
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 库位
         */
        private String warehouseLocation;
        /**
         * 仓位名称
         */
        private String warehouseLocationName;
        /**
         * 发货仓库id
         */
        private String deliveryWarehouseId;
        /**
         * 发货仓库名称
         */
        private String deliveryWarehouseName;

        /**
         * 一级供应商id
         */
        private String mainSupplierId;

        /**
         * 一级供应商名称
         */
        private String mainSupplierName;

        /**
         * 采购员
         */
        private String purchaseUserName;
    }

    /**
     * PDA:采购单查询
     */
    @Data
    @NoArgsConstructor
    public static class PdaPurchaseOrderDetail {
        /**
         * 主键id
         */
        private String id;
        /**
         * sku编号
         */
        private String skuNo;
        /**
         * 采购数量
         */
        private Integer purchaseQty;
        /**
         * 收货数量
         */
        private Integer receiveQty;
        /**
         * 入库数量
         */
        private Integer stockInQty;
    }

    @Data
    @NoArgsConstructor
    public static class PdaViewDTO {

        /**
         * id
         */
        private String id;

        /**
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 单位名称
         */
        private String unitName;

        /**
         * 是否加急（false否，true是）
         */
        private Boolean isUrgent;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 变体信息
         */
        private String variantProperty;

        /**
         * 报关型号
         */
        private String declareModel;

        /**
         * 报关名称
         */
        private String declareName;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 币别
         */
        private String currency;

        /**
         * 币别符号
         */
        private String currencySymbol;

        /**
         * 采购数量
         */
        private Integer purchaseQty;

        /**
         * 采购金额
         */
        private BigDecimal purchaseAmount;

        /**
         * 预计交货日期
         */
        private LocalDate planDeliveryDate;

        /**
         * 采购交期（天）
         */
        private Integer deliveryDay;

        /**
         * 是否是赠品（false否，true是）
         */
        private Boolean isGift;

        /**
         * 备注
         */
        private String remark;

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 委外子SKU下推可用
         */
        private String warehouseLocation;

        /**
         * 库位名称
         */
        private String warehouseLocationName;

        /**
         * 采购申请明细id(无需传值，后端使用)
         */
        private String purchaseApplicationDetailId;

        /**
         * 采购申请id(无需传值，后端使用)
         */
        private String purchaseApplicationId;

        /**
         * 签收数量
         */
        private Integer receiveQty;

        /**
         * 未交货数量
         */
        private Integer unReceiveQty;

        /**
         * 未交货数量
         */
        private Integer hasStockInQty;
    }

    /**
     * 明细id
     */
    @Data
    @NoArgsConstructor
    public static class PurchaseOrderConfirmDTO {
        private String id;
        /**
         * 明细id
         */
        private String detailId;
    }


    /**
     * 导入结束交货返回数据
     */
    @Data
    @NoArgsConstructor
    public static class ImportEndReceiveDTO {
        /**
         * 采购订单号
         */
        private String code;
        /**
         * 明细id
         */
        private String detailId;
        /**
         * SKU编码
         */
        private String skuNo;
    }
}
