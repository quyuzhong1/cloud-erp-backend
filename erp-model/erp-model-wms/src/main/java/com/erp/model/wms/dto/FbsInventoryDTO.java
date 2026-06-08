package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * FBS库存请求响应实体
 * </p>
 *
 * @author Cursor
 * @since 2026-05-25
 */
@Data
@NoArgsConstructor
public class FbsInventoryDTO implements Serializable {

    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {

        /**
         * 类型
         */
        private String tabFlag;

        /**
         * 数量
         */
        private Integer count;
    }

    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;

        /**
         * 不显示0库存
         */
        private Boolean isShowZeroInventory;
    }

    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 店铺Id
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 仓库Id
         */
        private String warehouseId;

        /**
         * 仓库
         */
        private String warehouseName;

        /**
         * 平台SKU
         */
        private String platformSku;

        /**
         * 平台产品名称
         */
        private String platformProductName;

        /**
         * FBS仓SKU
         */
        private String fbsSku;

        /**
         * 规格名称
         */
        private String specName;

        /**
         * SKUId
         */
        private String skuId;

        /**
         * SKU
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 履行/采购模式
         */
        private String purchaseMode;

        /**
         * 推荐补货数
         */
        private Integer recommendedReplenishmentQty;

        /**
         * 总库存
         */
        private Integer totalStockQty;

        /**
         * 已库收入库中
         */
        private Integer stockedInboundQty;

        /**
         * 转ASN入库中
         */
        private Integer transferAsnInboundQty;

        /**
         * 已预留
         */
        private Integer reservedQty;

        /**
         * 不可销售
         */
        private Integer unsellableQty;

        /**
         * 在途
         */
        private Integer inTransitQty;

        /**
         * 周转天数
         */
        private Integer turnoverDays;

        /**
         * 仓库库存能覆盖的销售天数
         */
        private Integer warehouseInventoryCoverageDays;

        /**
         * 销售速率（数量每天）
         */
        private BigDecimal dailyAvgSalesQty;

        /**
         * 最近7天销量
         */
        private Integer last7DaysSalesQty;

        /**
         * 最近15天销量
         */
        private Integer last15DaysSalesQty;

        /**
         * 最近30天销量
         */
        private Integer last30DaysSalesQty;

        /**
         * 最近60天销量
         */
        private Integer last60DaysSalesQty;

        /**
         * 最近90天销量
         */
        private Integer last90DaysSalesQty;

        /**
         * 库龄0-30天
         */
        private Integer stockAge030Qty;

        /**
         * 库龄31-60天
         */
        private Integer stockAge3160Qty;

        /**
         * 库龄61-90天
         */
        private Integer stockAge6190Qty;

        /**
         * 库龄91-120天
         */
        private Integer stockAge91120Qty;

        /**
         * 库龄121-180天
         */
        private Integer stockAge121180Qty;

        /**
         * 库龄大于180天
         */
        private Integer stockAgeOver180Qty;

        /**
         * 更新时间
         */
        private LocalDateTime platformUpdateTime;
    }

    /**
     * 导出Excel
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class ExportDTO extends PagingParamDTO {

        /**
         * 勾选的id集合
         */
        private List<String> ids;
    }

    /**
     * 列表汇总数量
     */
    @Data
    @NoArgsConstructor
    public static class SummaryNumber {

        /**
         * 推荐补货数
         */
        private Integer recommendedReplenishmentQty;

        /**
         * 总库存
         */
        private Integer totalStockQty;

        /**
         * 已库收入库中
         */
        private Integer stockedInboundQty;

        /**
         * 转ASN入库中
         */
        private Integer transferAsnInboundQty;

        /**
         * 已预留
         */
        private Integer reservedQty;

        /**
         * 不可销售
         */
        private Integer unsellableQty;

        /**
         * 在途
         */
        private Integer inTransitQty;
    }

    /**
     * 新增/同步
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class AddDTO extends CommonDTO {
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO implements Serializable {

        private String shopId;
        private String shopName;
        private String warehouseId;
        private String warehouseName;
        private String platformSku;
        private String platformProductName;
        private String fbsSku;
        private String specName;
        private String skuId;
        private String skuNo;
        private String productName;
        private String purchaseMode;
        private Integer recommendedReplenishmentQty;
        private Integer totalStockQty;
        private Integer stockedInboundQty;
        private Integer transferAsnInboundQty;
        private Integer reservedQty;
        private Integer unsellableQty;
        private Integer inTransitQty;
        private Integer turnoverDays;
        private Integer warehouseInventoryCoverageDays;
        private BigDecimal dailyAvgSalesQty;
        private Integer last7DaysSalesQty;
        private Integer last15DaysSalesQty;
        private Integer last30DaysSalesQty;
        private Integer last60DaysSalesQty;
        private Integer last90DaysSalesQty;
        private Integer stockAge030Qty;
        private Integer stockAge3160Qty;
        private Integer stockAge6190Qty;
        private Integer stockAge91120Qty;
        private Integer stockAge121180Qty;
        private Integer stockAgeOver180Qty;
        private LocalDateTime platformUpdateTime;
    }
}
