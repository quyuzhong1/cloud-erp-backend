package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class SoDeliveryNoticeDetailDTO {
    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class Add {
        /**
         * 发货数量
         */
        @NotNull(message = "发货数量不能为空")
        @Min(value = 1, message = "发货数量最小值为0")
        @Max(value = 999999999, message = "发货数量最大值为999999999")
        private Integer deliveryQty;
        /**
         * 出货仓库
         */
        private String warehouseId;
        /**
         * 是否关闭
         */
        private Boolean isClose;
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
         * 发货数量
         */
        @NotNull(message = "发货数量不能为空")
        @Min(value = 1, message = "发货数量最小值为0")
        @Max(value = 999999999, message = "发货数量最大值为999999999")
        private Integer deliveryQty;
        /**
         * 出货仓库
         */
        private String warehouseId;
        /**
         * 是否关闭
         */
        private Boolean isClose;
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
         * 发货数量
         */
        private Integer deliveryQty;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 是否关闭
         */
        private Boolean isClose;
        /**
         * 备注
         */
        private String remark;
    }
}
