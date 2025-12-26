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
}
