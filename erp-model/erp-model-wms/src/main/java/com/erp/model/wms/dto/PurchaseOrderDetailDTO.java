package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 采购订单明细表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-06-19
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
        * 币别
        */
        private String currency;

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
        * 收料组织id
        */
        private String receiveOrgId;

        /**
        * 收料组织名称
        */
        private String receiveOrgName;

        /**
        * 是否是赠品（false否，true是）
        */
        private Boolean isGift;

        /**
        * 备注
        */
        private String remark;

        /**
         * 执行状态
         */
        private String executionStatus;

        /**
        * 税率
        */
        private BigDecimal taxRate;

        /**
        * 签收数量
        */
        private Integer receiveQty;

        /**
        * 入库数量
        */
        private Integer stockInQty;

        /**
        * 交货数量
        */
        private Integer deliveryQty;

        /**
        * 退货数量
        */
        private Integer returnQty;

        /**
        * 是否加急（false否，true是）
        */
        private Boolean isUrgent;

        /**
        * 币种符号
        */
        private String currencySymbol;

        /**
        * 变体信息
        */
        private String variantProperty;

        /**
        * 是否结束收货
        */
        private Boolean isEndReceive;

        /**
        * 来源明细id
        */
        private String sourceDetailId;

        /**
        * 库位(委外可用)
        */
        private String warehouseLocation;


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
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 200,message = "产品名称最大长度不能超过200位")
        private String productName;

        /**
        * 报关型号
        */
        @NotBlank(message = "报关型号不能为空")
        @Size(max = 64,message = "报关型号最大长度不能超过64位")
        private String declareModel;

        /**
        * 报关名称
        */
        @NotBlank(message = "报关名称不能为空")
        @Size(max = 100,message = "报关名称最大长度不能超过100位")
        private String declareName;

        /**
        * 含税单价
        */
        @NotNull(message = "含税单价不能为空")
        @Digits(integer = 12, fraction = 4, message = "含税单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal taxPrice;

        /**
        * 币别
        */
        @NotBlank(message = "币别不能为空")
        @Size(max = 20,message = "币别最大长度不能超过20位")
        private String currency;

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
        * 收料组织id
        */
        @NotBlank(message = "收料组织id不能为空")
        @Size(max = 19,message = "收料组织id最大长度不能超过19位")
        private String receiveOrgId;

        /**
        * 收料组织名称
        */
        @NotBlank(message = "收料组织名称不能为空")
        @Size(max = 100,message = "收料组织名称最大长度不能超过100位")
        private String receiveOrgName;

        /**
        * 是否是赠品（false否，true是）
        */
        private Boolean isGift;

        /**
        * 备注
        */
        private String remark;

        /**
        * 到货状态（0未到货，1部分到货，2已到货）
        */
        @NotBlank(message = "到货状态（0未到货，1部分到货，2已到货）不能为空")
        @Size(max = 1,message = "到货状态（0未到货，1部分到货，2已到货）最大长度不能超过1位")
        private String arrivalStatus;

        /**
        * 到货时间
        */
        private LocalDateTime arrivalTime;

        /**
        * 税率
        */
        @NotNull(message = "税率不能为空")
        @Digits(integer = 12, fraction = 4, message = "税率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal taxRate;

        /**
        * 签收数量
        */
        @NotNull(message = "签收数量不能为空")
        private Integer receiveQty;

        /**
        * 入库数量
        */
        @NotNull(message = "入库数量不能为空")
        private Integer stockInQty;

        /**
        * 交货数量
        */
        @NotNull(message = "交货数量不能为空")
        private Integer deliveryQty;

        /**
        * 退货数量
        */
        @NotNull(message = "退货数量不能为空")
        private Integer returnQty;

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
        * 变体信息
        */
        @NotBlank(message = "变体信息不能为空")
        @Size(max = 64,message = "变体信息最大长度不能超过64位")
        private String variantProperty;

        /**
        * 是否结束收货
        */
        @NotNull(message = "是否结束收货不能为空")
        private Boolean isEndReceive;

        /**
        * 来源明细id
        */
        @NotBlank(message = "来源明细id不能为空")
        @Size(max = 19,message = "来源明细id最大长度不能超过19位")
        private String sourceDetailId;

        /**
        * 库位(委外可用)
        */
        @NotBlank(message = "库位(委外可用)不能为空")
        @Size(max = 32,message = "库位(委外可用)最大长度不能超过32位")
        private String warehouseLocation;


    }


}