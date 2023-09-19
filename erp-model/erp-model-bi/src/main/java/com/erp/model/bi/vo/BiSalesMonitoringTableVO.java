package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/29 17:28
 */
@Data
@NoArgsConstructor
public class BiSalesMonitoringTableVO {

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 排名
         */
        private Integer seq;

        /**
         * 品名
         */
        private String itemName;

        /**
         * 年累计销售额（或销量）
         */
        private BigDecimal sumYearSale;

        /**
         * 上个月累计销售额（或销量）
         */
        private BigDecimal sumFirstMonthSale;

        /**
         * 下个月累计销售额（或销量）
         */
        private BigDecimal sumSecondMonthSale;

        /**
         * 环比
         */
        private String relativeRatioName;

    }


    @Data
    @NoArgsConstructor
    public static class SkuDTO extends CommonDTO{

        /**
         * SKU
         */
        private String skuNo;

    }

    @Data
    @NoArgsConstructor
    public static class DeptDTO extends CommonDTO{

        /**
         * 部门
         */
        private String deptName;

    }

    @Data
    @NoArgsConstructor
    public static class ChargeDTO extends CommonDTO{

        /**
         * 负责人
         */
        private String chargeName;

    }

    @Data
    @NoArgsConstructor
    public static class CategoryDTO extends CommonDTO{

        /**
         * 品类名称
         */
        private String categoryName;

    }

    @Data
    @NoArgsConstructor
    public static class ShopDTO extends CommonDTO{

        /**
         * 店铺名称
         */
        private String shopName;

    }

    @Data
    @NoArgsConstructor
    public static class PlatformDTO extends CommonDTO{

        /**
         * 平台名称
         */
        private String platform;

    }

    @Data
    @NoArgsConstructor
    public static class CountryDTO extends CommonDTO{

        /**
         * 国家名称
         */
        private String countryName;

    }

}
