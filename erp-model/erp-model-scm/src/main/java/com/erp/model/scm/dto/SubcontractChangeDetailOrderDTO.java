package com.erp.model.scm.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 委外变单明细请求响应实体
 * </p>
 *
 * @author will
 * @since 2023-06-08
*/
@Data
@NoArgsConstructor
public class SubcontractChangeDetailOrderDTO implements Serializable {




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
        * 主表id
        */
        private String mainId;
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
        * 变更类型(操作类型)
        */
        private String optType;
        /**
        * 采购数量
        */
        private Integer qty;
        /**
        * 含税单价
        */
        private BigDecimal price;
        /**
        * 币别
        */
        private String currency;
        /**
        * 币种符号
        */
        private String currencySymbol;
        /**
        * 领料数量(发料数量)
        */
        private Integer deliveryQty;
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
        * 采购金额
        */
        private BigDecimal amount;
        /**
        * 原采购金额
        */
        private BigDecimal oldAmount;
        /**
        * 供应商id
        */
        private String supplierId;
        /**
        * 仓库id
        */
        private String warehouseId;
        /**
        * 仓库名称
        */
        private String warehouseName;
        /**
        * bom版本
        */
        private Integer bomVersion;
        /**
        * 变更备注
        */
        private String remark;
        /**
        * 来源明细id
        */
        private String sourceDetailId;
        /**
        * 父级SKUid
        */
        private String parentId;
        /**
        * 是否自动生成采购订单
        */
        private Boolean isGeneratePo;

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
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;
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
        @Size(max = 32,message = "sku编码最大长度不能超过32位")
        private String skuNo;
        /**
        * 变体信息
        */
        @NotBlank(message = "变体信息不能为空")
        @Size(max = 64,message = "变体信息最大长度不能超过64位")
        private String variantProperty;
        /**
        * 变更类型(操作类型)
        */
        @NotBlank(message = "变更类型(操作类型)不能为空")
        @Size(max = 32,message = "变更类型(操作类型)最大长度不能超过32位")
        private String optType;
        /**
        * 采购数量
        */
        @NotNull(message = "采购数量不能为空")
        private Integer qty;
        /**
        * 含税单价
        */
        @NotNull(message = "含税单价不能为空")
        @Digits(integer = 12, fraction = 4, message = "含税单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal price;
        /**
        * 币别
        */
        @NotBlank(message = "币别不能为空")
        @Size(max = 20,message = "币别最大长度不能超过20位")
        private String currency;
        /**
        * 币种符号
        */
        @NotBlank(message = "币种符号不能为空")
        @Size(max = 20,message = "币种符号最大长度不能超过20位")
        private String currencySymbol;
        /**
        * 领料数量(发料数量)
        */
        @NotNull(message = "领料数量(发料数量)不能为空")
        private Integer deliveryQty;
        /**
        * 原采购数量
        */
        @NotNull(message = "原采购数量不能为空")
        private Integer oldQty;
        /**
        * 原领料数量(发料数量)
        */
        @NotNull(message = "原领料数量(发料数量)不能为空")
        private Integer oldDeliveryQty;
        /**
        * 原含税单价
        */
        @NotNull(message = "原含税单价不能为空")
        @Digits(integer = 12, fraction = 4, message = "原含税单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal oldPrice;
        /**
        * 采购金额
        */
        @NotNull(message = "采购金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "采购金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal amount;
        /**
        * 原采购金额
        */
        @NotNull(message = "原采购金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "原采购金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal oldAmount;
        /**
        * 供应商id
        */
        @NotBlank(message = "供应商id不能为空")
        @Size(max = 19,message = "供应商id最大长度不能超过19位")
        private String supplierId;
        /**
        * 仓库id
        */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 19,message = "仓库id最大长度不能超过19位")
        private String warehouseId;
        /**
        * 仓库名称
        */
        @NotBlank(message = "仓库名称不能为空")
        @Size(max = 200,message = "仓库名称最大长度不能超过200位")
        private String warehouseName;
        /**
        * bom版本
        */
        @NotNull(message = "bom版本不能为空")
        private Integer bomVersion;
        /**
        * 变更备注
        */
        private String remark;
        /**
        * 来源明细id
        */
        @NotBlank(message = "来源明细id不能为空")
        @Size(max = 19,message = "来源明细id最大长度不能超过19位")
        private String sourceDetailId;
        /**
        * 父级SKUid
        */
        @NotBlank(message = "父级SKUid不能为空")
        @Size(max = 19,message = "父级SKUid最大长度不能超过19位")
        private String parentId;
        /**
        * 是否自动生成采购订单
        */
        @NotNull(message = "是否自动生成采购订单不能为空")
        private Boolean isGeneratePo;

    }


}