package com.erp.model.mrp.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class LocalHistoryInventoryDTO {

    /**
     * 即时库存分页列表
     */
    @Getter
    @Setter
    public static class PagingViewDTO {

        /**
         * sku id
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
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 可用库存数量
         */
        private Integer usableQty;
        /**
         * 待检库存数量
         */
        private Integer waitqcQty;
        /**
         * 冻结库存数量
         */
        private Integer frozenQty;

        /**
         * 采购在途
         */
        private Integer purchaseTransitQty;

        /**
         * 调拨在途
         */
        private Integer transferTransitQty;

        /**
         * 单据日期
         */
        private LocalDate billDate;
        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
    }

    /**
     * 即时库存查询条件
     */
    @Getter
    @Setter
    public static class SearchParamDTO extends SortDTO {

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
    public static class ExportDTO extends FbaHistoryInventoryDTO.PagingParamDTO {
        /**
         * 勾选的id集合
         */
        private List<String> ids;
    }


}
