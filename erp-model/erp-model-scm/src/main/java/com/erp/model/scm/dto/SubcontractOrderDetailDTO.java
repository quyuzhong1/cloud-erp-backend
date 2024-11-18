package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 委外订单明细请求响应实体
 *
 * @author will
 * @since 2023-06-08
*/
@Data
@NoArgsConstructor
public class SubcontractOrderDetailDTO implements Serializable {


    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO extends CommonDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * SKU编码
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
        * 变体信息
        */
        private String variantProperty;
        /**
        * 供应商名称
        */
        private String supplierName;

        /**
         * 币种
         */
        private String currency;


        /**
        * 币种符号
        */
        private String currencySymbol;

        /**
        * 采购金额
        */
        private BigDecimal amount;

        /**
        * 仓库名称
        */
        private String warehouseName;

        /**
        * bom版本
        */
        private String bomVersion;

        /**
         * 即时库存数量
         */
        private Integer curInventoryQty;

        /**
         * 付款条件名称
         */
        private String paymentConditionName;

        /**
         * 仓库库位名称
         */
        private String warehouseLocationName;

        /**
         * 子件集合
         */
        private List<ChildDTO> childList;
    }

    @Data
    @NoArgsConstructor
    public static class ChildDTO extends CommonDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * SKU编码
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 变体信息
         */
        private String variantProperty;
        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 币种
         */
        private String currency;


        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 采购金额
         */
        private BigDecimal amount;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * bom版本
         */
        private String bomVersion;

        /**
         * bom用量
         */
        private Integer quantity;

        /**
         * 即时库存数量
         */
        private Integer curInventoryQty;

        /**
         * 付款条件名称
         */
        private String paymentConditionName;


        /**
         * 库位名称
         */
        private String warehouseLocationName;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 子件集合
         */
        private List<AddDTO> childList;

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
        private String id;

        /**
         * 子件集合
         */
        private List<UpdateDTO> childList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO implements Serializable{

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
        * 供应商id
        */
        @Size(max = 19,message = "供应商id最大长度不能超过19位")
        private String supplierId;
        /**
        * 采购数量
        */
        @NotNull(message = "采购数量不能为空")
        private Integer qty;
        /**
        * 领料数量(发料数量)
        */
        @NotNull(message = "领料数量(发料数量)不能为空")
        private Integer deliveryQty;
        /**
        * 含税单价
        */
        @Digits(integer = 12, fraction = 4, message = "含税单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal price;

        /**
         * 税率
         */
        @Digits(integer = 16,fraction = 4,message = "税率最大16字符，小数位不能大于4个字符")
        private BigDecimal taxRate;

        /**
         * 价税合计
         */
        private BigDecimal amount;

        /**
        * 仓库id
        */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 19,message = "仓库id最大长度不能超过19位")
        private String warehouseId;

        /**
         * 库位
         */
        private String warehouseLocation;

        /**
        * 预计交货日期
        */
        private LocalDate planDeliveryDate;

        /**
         * 是否是赠品（false否，true是）
         */
        private Boolean isGift;

        /**
        * 是否加急（false否，true是）
        */
        @NotNull(message = "是否加急（false否，true是）不能为空")
        private Boolean isUrgent;

        /**
         * 备注
         */
        @Size(max = 255,message = "备注不能大于255字符")
        private String remark;

        /**
        * 是否自动生成采购订单
        */
        @NotNull(message = "是否自动生成采购订单不能为空")
        private Boolean isGeneratePo;

        /**
         * 是否自动生成入库单
         * 勾选即为当成品采购订单入库时，以相应BOM数量对子件数量自动入库。
         */
        @NotNull(message = "是否自动生成入库单不能为空")
        private Boolean isGenerateInStock;

        /**
        * 来源明细id
        */
        @Size(max = 19,message = "来源明细id最大长度不能超过19位")
        private String sourceDetailId;

        /**
         * 付款条件
         */
        @NotBlank(message = "付款条件不能为空")
        private String paymentCondition;


        private String kingdeeDetailId;
    }


}