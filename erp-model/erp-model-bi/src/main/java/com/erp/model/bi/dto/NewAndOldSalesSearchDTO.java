package com.erp.model.bi.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class NewAndOldSalesSearchDTO {

    /**
     * 查询参数
     **/
    @Data
    public static class SearchDTO {
        /**
         * 搜索类型：/bi/common/enumDropDown?type=NewAndOldSalesSearchType
         * 描述：division 事业部，
         *  user 人员，
         *  shop 店铺，
         *  category 品类
         */
        private String searchType;

        /**
         * 搜索内容
         */
        private String searchParam;
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
        private Integer oldSalesQuantity=0;
        /**
         * 新品销售额占比
         */
        private BigDecimal newProductSalesRatio;
        /**
         * 老品销售额占比
         */
        private BigDecimal oldProductSalesRatio;

        public PagingDTO() {
            this.newProductSales = BigDecimal.ZERO;
            this.newSalesQuantity = 0;
            this.oldProductSales = BigDecimal.ZERO;
            this.oldSalesQuantity = 0;
            this.newProductSalesRatio = BigDecimal.ZERO;
            this.oldProductSalesRatio = BigDecimal.ZERO;
        }
    }


}
