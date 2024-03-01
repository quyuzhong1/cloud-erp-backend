package com.erp.model.srm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
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
 * 采购订单明细表（已确认）请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2024-01-27
*/
@Data
@NoArgsConstructor
public class PurchaseOrderDetailDTO implements Serializable {




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
        * 产品名称
        */
        private String productName;

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
        * 是否是赠品（false否，true是）
        */
        private Boolean isGift;

        /**
        * 备注
        */
        private String remark;

        /**
        * 税率
        */
        private BigDecimal taxRate;

        /**
        * 是否加急（false否，true是）
        */
        private Boolean isUrgent;

        /**
        * 币种符号
        */
        private String currencySymbol;

        /**
        * 来源明细id
        */
        private String sourceDetailId;

        /**
        * 执行状态
        */
        private String executionStatus;

        /**
        * 确认类型（auto 系统，manual 手动）
        */
        private String confirmType;

        /**
        * 单据编号（订单单号）
        */
        private String code;

        /**
        * 客户联系人id
        */
        private String purchaseUserId;

        /**
        * 客户联系人名称
        */
        private String purchaseUserName;

        /**
        * 客户id
        */
        private String purchaseOrgId;

        /**
        * 客户名称
        */
        private String purchaseOrgName;

        /**
        * 目的仓库id
        */
        private String deliveryWarehouseId;

        /**
        * 目的仓库名称
        */
        private String deliveryWarehouseName;

        /**
        * 单据类型,字典PurchaseOrderType类型
        */
        private String type;


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
        * 采购订单id
        */
        @NotBlank(message = "采购订单id不能为空")
        @Size(max = 19,message = "采购订单id最大长度不能超过19位")
        private String purchaseOrderId;

        /**
        * skuId
        */
        private String skuId;

        /**
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 200,message = "产品名称最大长度不能超过200位")
        private String productName;

        /**
        * 采购数量
        */
        @NotNull(message = "采购数量不能为空")
        private Integer purchaseQty;

        /**
        * 采购金额
        */
        @NotNull(message = "采购金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "采购金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal purchaseAmount;

        /**
        * 预计交货日期
        */
        private LocalDate planDeliveryDate;

        /**
        * 是否是赠品（false否，true是）
        */
        private Boolean isGift;

        /**
        * 备注
        */
        private String remark;

        /**
        * 税率
        */
        @NotNull(message = "税率不能为空")
        @Digits(integer = 12, fraction = 4, message = "税率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal taxRate;

        /**
        * 是否加急（false否，true是）
        */
        @NotNull(message = "是否加急（false否，true是）不能为空")
        private Boolean isUrgent;

        /**
        * 币种符号
        */
        @NotBlank(message = "币种符号不能为空")
        @Size(max = 20,message = "币种符号最大长度不能超过20位")
        private String currencySymbol;

        /**
        * 来源明细id
        */
        @NotBlank(message = "来源明细id不能为空")
        @Size(max = 19,message = "来源明细id最大长度不能超过19位")
        private String sourceDetailId;

        /**
        * 执行状态
        */
        @NotBlank(message = "执行状态不能为空")
        @Size(max = 50,message = "执行状态最大长度不能超过50位")
        private String executionStatus;

        /**
        * 确认类型（auto 系统，manual 手动）
        */
        @NotBlank(message = "确认类型（auto 系统，manual 手动）不能为空")
        @Size(max = 32,message = "确认类型（auto 系统，manual 手动）最大长度不能超过32位")
        private String confirmType;

        /**
        * 客户联系人id
        */
        @NotBlank(message = "客户联系人id不能为空")
        @Size(max = 19,message = "客户联系人id最大长度不能超过19位")
        private String purchaseUserId;

        /**
        * 客户联系人名称
        */
        @NotBlank(message = "客户联系人名称不能为空")
        @Size(max = 64,message = "客户联系人名称最大长度不能超过64位")
        private String purchaseUserName;

        /**
        * 客户id
        */
        @NotBlank(message = "客户id不能为空")
        @Size(max = 19,message = "客户id最大长度不能超过19位")
        private String purchaseOrgId;

        /**
        * 客户名称
        */
        @NotBlank(message = "客户名称不能为空")
        @Size(max = 100,message = "客户名称最大长度不能超过100位")
        private String purchaseOrgName;

        /**
        * 目的仓库id
        */
        @NotBlank(message = "目的仓库id不能为空")
        @Size(max = 19,message = "目的仓库id最大长度不能超过19位")
        private String deliveryWarehouseId;

        /**
        * 目的仓库名称
        */
        @NotBlank(message = "目的仓库名称不能为空")
        @Size(max = 200,message = "目的仓库名称最大长度不能超过200位")
        private String deliveryWarehouseName;

        /**
        * 单据类型,字典PurchaseOrderType类型
        */
        @NotBlank(message = "单据类型,字典PurchaseOrderType类型不能为空")
        @Size(max = 32,message = "单据类型,字典PurchaseOrderType类型最大长度不能超过32位")
        private String type;


    }


}