package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class SoReturnNoticeDetailDTO {
    private SoReturnNoticeDetailDTO() {
        throw new IllegalStateException("Utility SoReturnNoticeDetailDTO class");
    }
    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class Add  extends Common{
        /**
         * 退货数量
         */
        @NotNull(message = "退货数量不能为空")
        @Min(value = 1, message = "退货数量最小值为1")
        @Max(value = 999999999, message = "退货数量最大值为999999999")
        private Integer returnQty;
        /**
         * 备注
         */
        private String remark;
        /**
         * 销售单明细表id
         */
        private String sourceDetailId;
        /**
         * 平台sku
         */
        private String platformSkuNo;
        /**
         * skuId
         */
        private String skuId;
        /**
         * 退货类型
         */
        private String returnTypeDict;
        /**
         * 退货原因
         */
        private String returnReasonDict;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class Update extends Common{
        /**
         * 明细id
         */
        private String id;
        /**
         * 主键id
         */
        private String mainId;
        /**
         * 退货数量
         */
        @NotNull(message = "退货数量不能为空")
        @Min(value = 1, message = "退货数量最小值为1")
        @Max(value = 999999999, message = "退货数量最大值为999999999")
        private Integer returnQty;
        /**
         * 备注
         */
        private String remark;
        /**
         * 销售单明细表id
         */
        private String sourceDetailId;
    }

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class Common {

        /**
         * skuId
         */
        private String skuId;
        /**
         * 平台sku
         */
        private String platformSkuNo;
        /**
         * 是否子sku
         */
        private Boolean isChildSkuNo;
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
        /**
         *汇率
         */
        private BigDecimal exchangeRate;

    }

    /**
     * 查询详情
     */
    @Data
    @NoArgsConstructor
    public static class View  extends Common{
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
         * 已出库数量
         */
        private Integer deliveryQty;
        /**
         * 退货数量
         */
        private Integer returnQty;
        /**
         * 退货类型
         */
        private String returnTypeDict;
        /**
         * 退货类型名称
         */
        private String returnTypeDictName;
        /**
         * 退货原因 调用字典接口 类型=ReturnReason
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
    }
}
