package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
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
    public static class GroupDTO extends BiFilterDTO{

        /**
         * 成本类型集合
         *
         * +
         */
        @NotEmpty(message = "成本数据不呢个为空")
        private List<String> costTypeList;
    }

}
