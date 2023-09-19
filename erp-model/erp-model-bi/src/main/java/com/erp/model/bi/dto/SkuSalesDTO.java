package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * @Description
 * @Author yl
 * @Date 2023-09-19 8:57
 */
@Data
public class SkuSalesDTO  implements Serializable {


    @Data
    @NoArgsConstructor
    public static class SearchSkuDTO extends BiFilterDTO{
        private String skuType;


        /**
         * 是否组合品 true 是
         *
         */
        @NotNull(message = "单品，组合品不能为空")
        private Boolean isCombo;
    }

    @Data
    @NoArgsConstructor
    public static class  PagingSalesInfoDTO{
        /**
         * 品名
         *
         */
        private String productName;

        /**
         * sku
         */
        private String skuNo;

        /**
         * 公司首单日期
         */
        private Date firstOrderDate;

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
         *订单统计
         */
        private Integer orderCount;


        /**
         * 客单价
         */
        private BigDecimal perCustomerTransaction=BigDecimal.ZERO;




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
    }

}
