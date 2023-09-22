package com.erp.model.bi.dto;

import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @version 1.0
 * @description: 成本DTO
 * @date 2023/9/19 12:09
 */
@Data
@NoArgsConstructor
public class BiDataSourceCostDTO {

    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 月份
         */
        private LocalDateTime month;

        /**
         * 部门id
         */
        private String deptId;

        /**
         * 部门名称
         */
        private String deptName;

        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 平台名称
         */
        private String platformName;

        /**
         * 站点
         */
        private String site;

        /**
         * 负责人id
         */
        private String chargeId;

        /**
         * 负责人名称
         */
        private String chargeName;

        /**
         * 成本数据
         */
        private Map<String, BigDecimal> map;

    }

    @Data
    @NoArgsConstructor
    public static class GroupDTO extends BiFilterDTO {

        /**
         * 成本类型集合
         * <p>
         * +
         */
        @NotEmpty(message = "成本数据不呢个为空")
        private List<String> costTypeList;
    }


    /**
     * 毛利率 ，毛利额 模块 入参
     */
    @Data
    @NoArgsConstructor
    public static class GrossProfitDTO extends BiFilterDTO {

        /**
         * 时间类型： 月: MONTH; 季度: QUARTER; 年: YEAR
         */
        @StateEnumValue(strValues = {"MONTH", "QUARTER", "YEAR"}, message = "时间类型有误")
        @NotNull(message = "时间类型不能为空")
        private String dateType;

    }


    /**
     * 值
     */
    @Data
    @NoArgsConstructor
    public static class DataValueDTO extends BiFilterDTO {

        /**
         * 时间 如2023-1  2023 第一季度
         */
        private String dateStr;

        /**
         * 对应值
         */
        private BigDecimal value;

        /**
         * 类型
         * grossProfit  毛利额
         * grossProfitRate 毛利率
         */
        private String type;

    }

}
