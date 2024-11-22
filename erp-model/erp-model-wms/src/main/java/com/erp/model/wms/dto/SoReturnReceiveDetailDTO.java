package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class SoReturnReceiveDetailDTO implements Serializable {
    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class Add {
        /**
         * skuId
         */
        private String skuId;
        /**
         * skuNo
         */
        private String skuNo;
        /**
         * 退货数量
         */
        @NotNull(message = "退货数量不能为空")
        @Min(value = 1, message = "退货数量最小值为1")
        @Max(value = 999999999, message = "退货数量最大值为999999999")
        private Integer returnQty;
        /**
         * 签收数量
         */
        @NotNull(message = "签收数量不能为空")
        @Min(value = 1, message = "签收数量最小值为1")
        @Max(value = 999999999, message = "签收数量最大值为999999999")
        private Integer receiveQty;
        /**
         * 备注
         */
        private String remark;
        /**
         * 退货单明细表id
         */
        private String sourceDetailId;

        /**
         * 通知单明细id
         */
        private String noticeDetailId;

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
         * 是否子skuNo
         */
        private Boolean isChildSkuNo;
        /**
         * 平台sku
         */
        private String platformSkuNo;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class Update {
        /**
         * skuId
         */
        private String skuId;
        /**
         * skuNo
         */
        private String skuNo;
        /**
         * id
         */
        private String id;
        /**
         * 退货数量
         */
        @NotNull(message = "退货数量不能为空")
        @Min(value = 1, message = "退货数量最小值为1")
        @Max(value = 999999999, message = "退货数量最大值为999999999")
        private Integer returnQty;
        /**
         * 签收数量
         */
        @NotNull(message = "签收数量不能为空")
        @Min(value = 1, message = "签收数量最小值为1")
        @Max(value = 999999999, message = "签收数量最大值为999999999")
        private Integer receiveQty;
        /**
         * 备注
         */
        private String remark;
        /**
         * 退货单明细表id
         */
        private String sourceDetailId;

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
         * 是否子skuNo
         */
        private Boolean isChildSkuNo;
        /**
         * 平台sku
         */
        private String platformSkuNo;
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
         * 变体信息
         */
        private String variantProperty;
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
         * 签收数量
         */
        private Integer receiveQty;
        /**
         * 退货类型 wms/common/enumDropDown?type=ReturnType
         * 描述：refund 退货扣款 replenishment 退货补货
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
        /**
         * 销售单明细表id
         */
        private String sourceDetailId;
        /**
         * 是否子skuNo
         */
        private Boolean isChildSkuNo;
        /**
         * 平台sku
         */
        private String platformSkuNo;
    }
}
