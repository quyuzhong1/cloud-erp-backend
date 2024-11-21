package com.erp.model.mrp.dto;

import com.common.business.dto.AdvanceQueryDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class OverseasHistoryInventoryDTO {


    @Getter
    @Setter
    public static class ListDTO {

        /**
         * 主键id
         */
        private String  id;

        /**
         * 平台仓库编码
         */
        private String warehouseCode;

        /**
         * 平台类型: goodcang=谷仓，iml=艾姆勒
         */
        private String dictPlatform;

        /**
         * 仓库名称
         */
        private String name;
        /**
         * ERP系统产品名称
         */
        private String productName;

        /**
         * ERP的SKU
         */
        private String skuNo;

        /**
         * ERP的SKU ID
         */
        private String skuId;

        /**
         * 待上架数量
         */
        private Integer pendingQty;

        /**
         * 可售数量
         */
        private Integer sellableQty;

        /**
         * 不可售数量
         */
        private Integer unsellableQty;

        /**
         * 待出库数量
         */
        private Integer reservedQty;

        /**
         * 冻结数量
         */
        private Integer frozenQty;

        /**
         * 历史出库数量
         */
        private Integer shippedQty;

        /**
         * 平台下载更新时间 (更新时间)
         */
        private LocalDateTime downloadTime;

        /**
         * 单据日期
         */
        private LocalDate billDate;
    }

    @Getter
    @Setter
    public static class PagingParamDTO {
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
