package com.erp.model.mrp.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 备货系数（规则设置）请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-08-23
*/
@Data
@NoArgsConstructor
public class CfgRuleStockingRatioDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 备货主表id
        */
        private String stockUpId;

        /**
        * 排序字段
        */
        private Integer index;

        /**
        * 名称
        */
        private String name;

        /**
         * 日期数组
         */
        private List<LocalDate> dateList;

        /**
        * 备货系数
        */
        private BigDecimal stockingRatio;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 排序字段
        */
        @NotNull(message = "排序字段不能为空")
        private Integer index;

        /**
        * 名称
        */
        @NotBlank(message = "名称不能为空")
        @Size(max = 10,message = "名称最大长度不能超过10位")
        private String name;

        /**
         * 日期数组
         */
        private List<LocalDate> dateList;

        /**
        * 备货系数
        */
        @NotNull(message = "备货系数不能为空")
        @Digits(integer = 12, fraction = 4, message = "备货系数整数位不能超过12位，小数位不能超过4位")
        private BigDecimal stockingRatio;


    }

    @Getter
    @Setter
    public static class StockingRatioResultDTO {

        /**
         * 排序字段
         */
        private Integer index;
        /**
         * 名称
         */
        private String name;
        /**
         * 开始时间
         */
        private LocalDate startDate;
        /**
         * 结束日期
         */
        private LocalDate endDate;
        /**
         * 备货系数
         */
        private BigDecimal stockingRatio;

        /**
         * 类型，conventional常规品，new新品
         */
        private String type;
    }

    @Data
    @NoArgsConstructor
    public static class StockingRatioExportDTO {
        /**
         * 平台
         */
        private String platform;

        /**
         * SKU
         */
        private String skuNo;

        /**
         * 店铺
         */
        private String shopName;

        /**
         * 规则名称
         */
        private String name;

        /**
         * 开始日期
         */
        private LocalDate startDate;

        /**
         * 结束日期
         */
        private LocalDate endDate;

        /**
         * 备货系数
         */
        private BigDecimal stockingRatio;
    }
}