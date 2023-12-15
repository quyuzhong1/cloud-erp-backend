package com.erp.model.bi.dto;

import com.erp.model.bi.vo.LabelVO;
import com.erp.model.bi.vo.SalesBaseVO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @Description
 * @Author yl
 * @Date 2023-09-19 8:57
 */
@Data
public class SkuSalesDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class SearchSkuDTO extends BiFilterDTO {
        private String skuType;
    }

    @Data
    public static class PagingSalesInfoDTO {
        /**
         * 品名
         */
        private String productName;

        /**
         * sku
         */
        private String skuNo;
        /**
         * sku
         */
        private String skuId;

        /**
         * 公司首单日期
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate firstOrderDate;

        /**
         * 销售状态
         */
        private Integer saleState;


        /**
         * 销售状态名
         */
        private String saleStateName;


        /**
         * 销售额
         */
        private BigDecimal sales;


        /**
         * 销量
         */
        private Integer salesQty;


        /**
         * 订单统计
         */
        private Integer orderCount;


        /**
         * 客单价
         */
        private BigDecimal perCustomerTransaction;


        /**
         * 近七日销量
         */
        private Integer lastSevenDaysSalesQty;

        /**
         * 近三十天日销量
         */
        private Integer lastThirtyDaysSalesQty;


        /**
         * 销售趋势
         */
        private List<Integer> salesTrend;
        /**
         * 销售趋势 带时间
         */
        private List<SalesBaseVO> salesTrendList;

        /**
         * 标签列表
         */
        private List<LabelVO> labels;

        /**
         * 是否新品 true 是 false 不是
         */
        private Boolean isNewProduct;

        private String isNewProductName;

        public PagingSalesInfoDTO() {
            this.isNewProduct = Boolean.FALSE;
            this.isNewProductName = "否";
            this.perCustomerTransaction = BigDecimal.ZERO;
        }
    }


    /**
     * 产品 标识销售
     */
    @Data
    public static class ProductFlagSalesDTO {
        /**
         * 等级 或者新老品
         */
        private String flag;

        /**
         * 销售额
         */
        private BigDecimal sales;


    }


    @Data
    @NoArgsConstructor
    public static class ProductSkuDTO {

        private String skuId;

        private String skuNo;

        private String productId;

        private String categoryId;
    }


}
