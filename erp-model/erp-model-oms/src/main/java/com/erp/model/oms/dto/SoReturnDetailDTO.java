package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class SoReturnDetailDTO {
    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class Add {
        /**
         * 退货数量
         */
        @NotNull(message = "退货数量不能为空")
        @Min(value = 1, message = "退货数量最小值为1")
        @Max(value = 999999999, message = "退货数量最大值为999999999")
        private Integer returnQty;
        /**
         * 退货类型 wms/common/enumDropDown?type=ReturnType
         * 描述：refund 退货扣款 replenishment 退货补货
         */
        private String returnTypeDict;
        /**
         * 退货原因 wms/common/enumDropDown?type=ReturnReason
         */
        private String returnReasonDict;
        /**
         * 备注
         */
        private String remark;
        /**
         * 销售单明细表id
         */
        private String sourceDetailId;

        /**
         * skuId
         */
        private String skuId;
        /**
         * skuNo
         */
        private String skuNo;
        /**
         * listing_id
         */
        private String listingId;
        /**
         * 平台sku
         */
        private String platformSkuNo;
        /**
         * 平台sku名称
         */
        private String platformSkuName;

        /**
         *退货金额
         */
        private BigDecimal returnAmount;
        /**
         *含税退货金额
         */
        private BigDecimal taxReturnAmount;
        /**
         *退货金额（本位币）
         */
        private BigDecimal returnAmountLocalCurrency;
        /**
         *含税退货金额（本位币）
         */
        private BigDecimal taxReturnAmountLocalCurrency;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class Update {
        /**
         * 主键id
         */
        private String id;
        /**
         * 销售数量
         */
        @NotNull(message = "销售数量不能为空")
        @Min(value = 1, message = "销售数量最小值为1")
        @Max(value = 999999999, message = "销售数量最大值为999999999")
        private Integer salesQty;
        /**
         * 退货数量
         */
        @NotNull(message = "退货数量不能为空")
        @Min(value = 1, message = "退货数量最小值为1")
        @Max(value = 999999999, message = "退货数量最大值为999999999")
        private Integer returnQty;
        /**
         * 退货类型 wms/common/enumDropDown?type=ReturnType
         * 描述：refund 退货扣款 replenishment 退货补货
         */
        private String returnTypeDict;
        /**
         * 退货原因 wms/common/enumDropDown?type=ReturnReason
         */
        private String returnReasonDict;
        /**
         * 备注
         */
        private String remark;
        /**
         * 销售单明细表id
         */
        private String sourceDetailId;

        /**
         * skuId
         */
        private String skuId;
        /**
         * skuNo
         */
        private String skuNo;
        /**
         * listing_id
         */
        private String listingId;
        /**
         * 平台sku
         */
        private String platformSkuNo;
        /**
         * 平台sku名称
         */
        private String platformSkuName;
        /**
         *退货金额
         */
        private BigDecimal returnAmount;
        /**
         *含税退货金额
         */
        private BigDecimal taxReturnAmount;
        /**
         *退货金额（本位币）
         */
        private BigDecimal returnAmountLocalCurrency;
        /**
         *含税退货金额（本位币）
         */
        private BigDecimal taxReturnAmountLocalCurrency;
    }

    /**
     * 查询详情
     */
    @Data
    @NoArgsConstructor
    public static class View {
        /**
         * id
         */
        private String id;
        /**
         * 主表id
         */
        private String mainId;
        /**
         * 来源明细id
         */
        private String sourceDetailId;
        /**
         * sku表id
         */
        private String skuId;
        /**
         * sku编号
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 销售数量
         */
        private Integer salesQty;
        /**
         * 退货数量
         */
        private Integer returnQty;
        /**
         * 已出库数量
         */
        private Integer deliveryQty;
        /**
         * 剩余未出数量
         */
        private Integer unDeliveryQty;
        /**
         * 销售金额
         */
        private BigDecimal salesAmount;
        /**
         * 币别
         */
        private String currency;
        /**
         * 币种符号
         */
        private String currencySymbol;
        /**
         * 退货类型
         */
        private String returnTypeDict;
        /**
         * 退货原因
         */
        private String returnReasonDict;
        /**
         * 退货原因名称
         */
        private String returnReasonDictName;
        /**
         * 备注
         */
        private String remark;
        /**
         *退货金额
         */
        private BigDecimal returnAmount;
        /**
         *含税退货金额
         */
        private BigDecimal taxReturnAmount;
        /**
         *退货金额（本位币）
         */
        private BigDecimal returnAmountLocalCurrency;
        /**
         *含税退货金额（本位币）
         */
        private BigDecimal taxReturnAmountLocalCurrency;
    }
}
