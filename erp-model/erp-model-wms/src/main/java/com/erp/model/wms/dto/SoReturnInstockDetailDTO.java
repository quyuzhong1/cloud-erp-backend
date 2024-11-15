package com.erp.model.wms.dto;

import com.erp.model.wms.entity.SoReturnInstockDetailEntity;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class SoReturnInstockDetailDTO {
    private SoReturnInstockDetailDTO() {
        throw new IllegalStateException("Utility SoReturnInstockDetailDTO class");
    }
    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClearSoReturnAndUpdateDTO {

        private List<String> clearSoReturnDetailIds;

        private List<SoReturnInstockEntity> updateMainList;

        private List<SoReturnInstockDetailEntity> updateList;
    }
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
         * 实退数量
         */
        @NotNull(message = "实退数量不能为空")
        @Min(value = 1, message = "实退数量最小值为1")
        @Max(value = 999999999, message = "实退数量最大值为999999999")
        private Integer realQty;
        /**
         * 签收数量
         */
        @NotNull(message = "签收数量不能为空")
        @Min(value = 1, message = "签收数量最小值为1")
        @Max(value = 999999999, message = "签收数量最大值为999999999")
        private Integer receiveQty;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓位
         */
        private String warehouseLocation;
        /**
         * 备注
         */
        private String remark;
        /**
         * 来源明细id
         */
        private String sourceDetailId;
        /**
         * 退货单明细表id
         */
        private String soReturnDetailId;
        /**
         * 退货类型 wms/common/enumDropDown?type=ReturnType
         * 描述：refund 退货扣款 replenishment 退货补货
         */
        private String returnTypeDict;
        /**
         * 退货原因 调用字典接口 类型=ReturnReason
         */
        private String returnReasonDict;
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
         * skuid
         */
        private String skuId;
        /**
         * skuNo
         */
        private String skuNo;
        /**
         * 实退数量
         */
        @NotNull(message = "实退数量不能为空")
        @Min(value = 1, message = "实退数量最小值为1")
        @Max(value = 999999999, message = "实退数量最大值为999999999")
        private Integer realQty;
        /**
         * 签收数量
         */
        @NotNull(message = "签收数量不能为空")
        @Min(value = 1, message = "签收数量最小值为1")
        @Max(value = 999999999, message = "签收数量最大值为999999999")
        private Integer receiveQty;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓位
         */
        private String warehouseLocation;
        /**
         * 备注
         */
        private String remark;
        /**
         * 来源明细id
         */
        private String sourceDetailId;
        /**
         * 退货单明细表id
         */
        private String soReturnDetailId;
        /**
         * 退货类型 wms/common/enumDropDown?type=ReturnType
         * 描述：refund 退货扣款 replenishment 退货补货
         */
        private String returnTypeDict;
        /**
         * 退货原因 调用字典接口 类型=ReturnReason
         */
        private String returnReasonDict;
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
         * spu编号
         */
        private String spuNo;
        /**
         * 单位
         */
        private String unitName;
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
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 仓位
         */
        private String warehouseLocation;
        /**
         * 仓位名称
         */
        private String warehouseLocationName;
        /**
         * 备注
         */
        private String remark;
        /**
         * 销售单明细表id
         */
        private String sourceDetailId;

        /**
         * 销售退货明细表id
         */
        private String soReturnDetailId;
        /**
         * 金额
         */
        private BigDecimal amount;
        /**
         * 币种
         */
        private String currency;
    }
}
