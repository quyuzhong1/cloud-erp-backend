package com.erp.model.wms.dto;


import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class ReportProcessingDTO implements Serializable {


    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

    }


    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        private String type;
        /**
         * 业务类型名称
         */
        private String typeName;
        /**
         * skuId
         */
        private String skuId;
        /**
         * skuNo
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;
        /**
         * 冻结总数
         */
        private Integer totalFrozenQty;
        /**
         * 0-10天
         */
        private Integer tenDaysCount;

        /**
         * 11-20天
         */
        private Integer twentyDaysCount;

        /**
         * 21-30天
         */
        private Integer thirtyDaysCount;
        /**
         * 31-60天
         */
        private Integer sixtyDaysCount;
        /**
         * 61-90天
         */
        private Integer ninetyDaysCount;
        /**
         * 90天以上
         */
        private Integer aboveNinetyDaysCount;


    }
}
