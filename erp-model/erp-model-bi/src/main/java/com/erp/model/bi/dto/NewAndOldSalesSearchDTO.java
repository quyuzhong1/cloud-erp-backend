package com.erp.model.bi.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class NewAndOldSalesSearchDTO {

    /**
     * 查询参数
     **/
    @Data
    public static class SearchDTO extends BiFilterDTO {
        /**
         * 搜索类型：/bi/common/enumDropDown?type=TargetMetricsSearchType
         * 描述：dept 事业部，
         *  user 人员，
         */
        private String searchType;

        /**
         * 搜索内容
         */
        private String searchParam;

        /**
         * 日期年月
         */
        private String yearMonth;
    }

    /**
     * 列表信息
     **/
    @Data
    public static class PagingDTO {

        /**
         * 平台名称
         */
        private String name;
        /**
         * 新品销售额
         */
        private BigDecimal newProductSales;
        /**
         * 新品销售量
         */
        private Integer newSalesQuantity;
        /**
         * 老品销售额
         */
        private BigDecimal oldProductSales;
        /**
         * 老品销售量
         */
        private Integer oldSalesQuantity;
        /**
         * 新品销售额占比
         */
        private BigDecimal newProductSalesRatio;
        /**
         * 老品销售额占比
         */
        private BigDecimal oldProductSalesRatio;

        /**
         * 新品销售额完成率
         */
        private BigDecimal newSalesAmountFinishRate;

        /**
         * 新品销量完成率
         */
        private BigDecimal newSalesQuantityFinishRate;

        /**
         * 新品销售占比完成率
         */
        private BigDecimal newSalesRateFinishRate;

        /**
         * 新品销售目标
         */
        private BigDecimal newSalesAmountTarget;

        /**
         * 新品销量目标
         */
        private BigDecimal newSalesQuantityTarget;

        /**
         * 新品销售占比目标
         */
        private BigDecimal newSalesRateTarget;

        public PagingDTO() {
            this.newProductSales = BigDecimal.ZERO;
            this.newSalesQuantity = 0;
            this.oldProductSales = BigDecimal.ZERO;
            this.oldSalesQuantity = 0;
            this.newSalesAmountTarget = BigDecimal.ZERO;
            this.newSalesQuantityTarget = BigDecimal.ZERO;
            this.newSalesRateTarget = BigDecimal.ZERO;
            this.oldProductSalesRatio = BigDecimal.ZERO;
            this.newSalesAmountFinishRate = BigDecimal.ZERO;
            this.newSalesQuantityFinishRate = BigDecimal.ZERO;
            this.newSalesRateFinishRate = BigDecimal.ZERO;

        }
    }


}
