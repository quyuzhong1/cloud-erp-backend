package com.erp.model.mrp.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class FbaHistoryInventoryDTO {


    @Getter
    @Setter
    public static class ListDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 平台唯一编号
         */
        private String fbaShipmentId;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 平台sku
         */
        private String asin;

        /**
         * 卖家sku
         */
        private String msku;

        /**
         * fnSku
         */
        private String fnSku;

        /**
         * ERP的SKU
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * FBM可售
         */
        private String fbmFulfillableQty;

        /**
         * 计划入库数量
         */
        private Integer inboundWorkingQty;

        /**
         * 已发货数量
         */
        private Integer inboundShippedQty;

        /**
         * 入库中数量
         */
        private Integer inboundReceivingQty;

        /**
         * FBI可售
         */
        private Integer fulfillableQty;

        /**
         * 预留
         */
        private Integer reservedQty;

        /**
         * 调查中数量
         */
        private Integer researchingQty;

        /**
         * 不可售数量
         */
        private Integer unsellableQty;

        /**
         * 单据日期
         */
        private LocalDate billDate;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
    }

    @Getter
    @Setter
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        /**
         * 是否显示0库存
         */
        private Boolean isShowZeroInventory;

        /**
         * 单据日期
         */
        private LocalDate billDate;
    }


    @Getter
    @Setter
    public static class ExportDTO extends PagingParamDTO {
        /**
         * 勾选的id集合
         */
        private List<String> ids;
    }
}
