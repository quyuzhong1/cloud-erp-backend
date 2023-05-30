package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class SoReturnInstockDetailDTO {
    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class Add {

        /**
         * 实退数量
         */
        @NotNull(message = "实退数量不能为空")
        @Min(value = 1, message = "实退数量最小值为1")
        @Max(value = 999999999, message = "实退数量最大值为999999999")
        private Integer realQty;
        /**
         * 退货类型 wms/common/enumDropDown?type=ReturnType
         * 描述：refund 退货扣款 replenishment 退货补货
         */
        private String returnTypeDict;
        /**
         * 退货原因
         */
        private String returnReasonDict;
        /**
         * 仓位
         */
        private String warehouseLocation;
        /**
         * 备注
         */
        private String remark;
        /**
         * 退货单明细表id
         */
        private String sourceDetailId;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class Update {
        /**
         * id
         */
        private String id;

        /**
         * 实退数量
         */
        @NotNull(message = "实退数量不能为空")
        @Min(value = 1, message = "实退数量最小值为1")
        @Max(value = 999999999, message = "实退数量最大值为999999999")
        private Integer realQty;
        /**
         * 退货类型 wms/common/enumDropDown?type=ReturnType
         * 描述：refund 退货扣款 replenishment 退货补货
         */
        private String returnTypeDict;
        /**
         * 退货原因 调用字典接口 类型=ReturnReason
         */
        private String returnReasonDict;
        /**
         * 仓位
         */
        private String warehouseLocation;
        /**
         * 备注
         */
        private String remark;
        /**
         * 退货单明细表id
         */
        private String sourceDetailId;
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
         * skuId
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
         * 已出库数量
         */
        private Integer deliveryQty;
        /**
         * 应退数量
         */
        private Integer mustQty;
        /**
         * 签收数量
         */
        private Integer receiveQty;
        /**
         * 实退数量
         */
        private Integer realQty;
        /**
         * 退货类型
         */
        private String returnTypeDict;
        /**
         * 退货原因 调用字典接口 类型=ReturnReason
         */
        private String returnReasonDict;
        /**
         * 仓位
         */
        private String warehouseLocation;
        /**
         * 备注
         */
        private String remark;
        /**
         * 销售单明细表id
         */
        private String sourceDetailId;
    }
}
