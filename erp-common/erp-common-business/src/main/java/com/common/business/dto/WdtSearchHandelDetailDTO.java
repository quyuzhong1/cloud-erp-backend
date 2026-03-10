package com.common.business.dto;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 虚拟仓分货单推送
 */
@Data
@NoArgsConstructor
public class WdtSearchHandelDetailDTO {


    /**
     * 查询虚拟仓库存信息
     */
    @Data
    @NoArgsConstructor
    public static class SearchVirtualInventoryParamDTO {

        /**
         * 开始时间 格式：yyyy-MM-dd HH:mm:ss
         */
        private String start_time;
        /**
         * 结束时间 格式：yyyy-MM-dd HH:mm:ss
         */
        private String end_time;
        /**
         * 商家编码
         */
        private String spec_nos;
        /**
         * 虚拟仓编号
         */
        private String virtual_warehouse_no;
        /**
         * 实体仓编号
         */
        private String warehouse_no;
    }


    @Data
    @NoArgsConstructor
    public static class SearchVirtualInventoryDTO {
        /**
         * 商品编码
         */
        @Alias("spec_no")
        private String skuNo;
        /**
         * 规格名称
         */
        @Alias("spec_name")
        private String productName;
        /**
         * 虚拟仓编号
         */
        @Alias("vir_warehouse_no")
        private String virtualWarehouseCode;
        /**
         * 虚拟仓名称
         */
        @Alias("vir_warehouse_name")
        private String virtualWarehouseName;
        /**
         * 实体仓编号
         */
        @Alias("warehouse_no")
        private String warehouseCode;
        /**
         * 实体仓名称
         */
        @Alias("warehouse_name")
        private String warehouseName;
        /**
         * 可用数量
         */
        @Alias("can_use_num")
        private String qty;
    }



    /**
     * 查询虚拟仓信息
     */
    @Data
    @NoArgsConstructor
    public static class SearchVirtualWarehouseParamDTO {

        /**
         * 开始时间 格式：yyyy-MM-dd HH:mm:ss
         */
        private String start_time;
        /**
         * 结束时间 格式：yyyy-MM-dd HH:mm:ss
         */
        private String end_time;
        /**
         * 单据类型
         * 1：锁定入库
         * 2：释放出库
         * 3：调拨
         * 4：采购入库
         */
        private int order_type;
        /**
         * 虚拟仓编号
         */
        private String virtual_warehouse_no;
        /**
         * 实体仓编号
         */
        private String warehouse_no;
    }

    @Data
    @NoArgsConstructor
    public static class SearchVirtualWarehouseDTO {
        /**
         * 虚拟仓编号
         */
        @Alias("virtual_warehouse_no")
        private String virtualWarehouseCode;

        /**
         * 调入虚拟仓编号
         */
        @Alias("to_virtual_warehouse_no")
        private String toVirtualWarehouseCode;

        /**
         * 单据类型
         * 1：锁定入库
         * 2：释放出库
         * 3：调拨
         * 4：采购入库
         */
        @Alias("order_type")
        private int orderType;

        /**
         * 审核状态
         * 10：编辑中
         * 20：待审核
         * 25：待自动审核
         * 30：已审核
         * 90：已取消
         */
        @Alias("order_status")
        private String orderStatus;

        /**
         * 备注
         */
        @Alias("remark")
        private String remark;
    }
}
