package com.erp.model.bi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Will
 * @version 1.0
 * @description: 目标完成DTO
 * @date 2023/9/14 16:14
 */
@Data
@NoArgsConstructor
public class TargetFinishDTO implements Serializable {

    /**
     * 列表参数
     */
    @Data
    @NoArgsConstructor
    public static class ParamDTO extends BiFilterDTO{

        /**
         * 年份
         */
        private String year;

        /**
         * http://172.16.100.11:3002/project/74/interface/api/23416 type = metrics
         * 考核维度（财务销售额、销售额、净销售额、销量、毛利润、毛利率）
         */
        @NotBlank(message = "考核维度不能为空")
        private String metrics;
        /**
         * http://172.16.100.11:3002/project/74/interface/api/23416 type = targetViewType
         *
         * 查看类型（完成率 finishRate、占比 ratio）
         */
        @NotBlank(message = "查看类型不能为空")
        private String viewType;
        /**
         * http://172.16.100.11:3002/project/74/interface/api/23416 type = targetSearchType
         * 搜索类型（二级部门 dept、人员 user、店铺 shop,、品类 category、SKU sku）
         */
        @NotBlank(message = "搜索类型不能为空")
        private String searchType;
        /**
         * 时间类型 dateTypeEnum
         */
        private String dateType;
    }


    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 类型Id(仅部门类型时后端使用)
         */
        private String typeId;

        /**
         * 类型名称 （部门、人员、店铺、品类、SKU）
         */
        private String typeName;

        /**
         * 月份
         */
        private Integer month;

        /**
         * 对应值
         */
        private BigDecimal value;
    }


    @Data
    @NoArgsConstructor
    public static class TotalSlotDTO {

        /**
         * 累计年度目标
         */
        private BigDecimal yearTotalTarget;
        /**
         * 累计年度实际
         */
        private BigDecimal yearTotalReal;
        /**
         * 完成率
         */
        private BigDecimal rate;
    }

    @Data
    @NoArgsConstructor
    public static class SlotDTO {

        /**
         * 目标值
         */
        private BigDecimal targetValue;
        /**
         * 销量/销售额/利润/利润率
         */
        private BigDecimal value;

        /**
         * 完成率/比例
         */
        private BigDecimal rate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupViewDTO {

        /**
         * 分组数据
         */
        private String groupData;

        /**
         * 显示数据
         */
        private String viewData;
    }


}
