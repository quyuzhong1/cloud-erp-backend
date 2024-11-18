package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 委外变单明细请求响应实体
 *
 * @author will
 * @since 2023-06-08
*/
@Data
@NoArgsConstructor
public class SubcontractChangeDetailDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO extends CommonDTO{

        /**
        * 主键id
        */
        private String  id;

        /**
         * 产品名称
         */
        private String productName;

        /**
        * 变体信息
        */
        private String variantProperty;

        /**
        * 币别
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
         * 操作名称
         */
        private String optTypeName;

        /**
         * 供应商名称
         */
        private String supplierName;
        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
        * 原采购数量
        */
        private Integer oldQty;

        /**
        * 原领料数量(发料数量)
        */
        private Integer oldDeliveryQty;

        /**
        * 原含税单价
        */
        private BigDecimal oldPrice;

        /**
        * 原采购金额
        */
        private BigDecimal oldAmount;

        /**
        * bom版本
        */
        private String bomVersion;

        /**
         * 采购数量
         */
        private Integer qty;

        /**
         * 明细子集
         */
        private List<ChildDTO> childList;
    }

    @Data
    @NoArgsConstructor
    public static class ChildDTO extends CommonDTO{

        /**
         * 主键id
         */
        private String  id;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 变体信息
         */
        private String variantProperty;

        /**
         * 币别
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
         * 操作名称
         */
        private String optTypeName;

        /**
         * 供应商名称
         */
        private String supplierName;
        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 原采购数量
         */
        private Integer oldQty;

        /**
         * 原领料数量(发料数量)
         */
        private Integer oldDeliveryQty;

        /**
         * 原含税单价
         */
        private BigDecimal oldPrice;

        /**
         * 原采购金额
         */
        private BigDecimal oldAmount;

        /**
         * bom版本
         */
        private String bomVersion;

        /**
         * bom用量
         */
        private Integer quantity;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        /**
         * 明细子集
         */
        @NotEmpty(message = "明细子级SKU不能为空")
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
         * 明细子集
         */
        @NotEmpty(message = "明细子级SKU不能为空")
        private List<UpdateDTO> childList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO implements Serializable {

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;
        /**
        * sku编码
        */
        @NotBlank(message = "sku编码不能为空")
        @Size(max = 64,message = "sku编码最大长度不能超过64位")
        private String skuNo;

        /**
        * 变更类型(操作类型)
        */
        @NotBlank(message = "变更类型(操作类型)不能为空")
        @Size(max = 32,message = "变更类型(操作类型)最大长度不能超过32位")
        private String optType;

        /**
         * 供应商id
         */
        @Size(max = 19,message = "供应商id最大长度不能超过19位")
        private String supplierId;
        /**
         * 仓库id
         */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 19,message = "仓库id最大长度不能超过19位")
        private String warehouseId;


        /**
        * 含税单价
        */
        @Digits(integer = 12, fraction = 4, message = "含税单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal price;

        /**
        * 币别
        */
        @NotBlank(message = "币别不能为空")
        @Size(max = 20,message = "币别最大长度不能超过20位")
        private String currency;

        /**
        * 领料数量(发料数量)
        */
        @NotNull(message = "领料数量(发料数量)不能为空")
        private Integer deliveryQty;

        /**
        * 变更备注
        */
        private String remark;

        /**
        * 来源明细id
        */
        @Size(max = 19,message = "来源明细id最大长度不能超过19位")
        private String sourceDetailId;

        /**
        * 是否自动生成采购订单
        */
        @NotNull(message = "是否自动生成采购订单不能为空")
        private Boolean isGeneratePo;

        /**
         * 预计交货日期
         */
        private LocalDate planDeliveryDate;

        /**
         * 付款条件
         */
        private String paymentCondition;

        /**
         * 库位
         */
        private String warehouseLocation;
    }


}