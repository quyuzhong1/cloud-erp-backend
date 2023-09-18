package com.erp.model.bi.dto;

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
         * 考核维度（财务销售额、销售额、净销售额、销量、毛利润、毛利率）
         */
        @NotBlank(message = "考核维度不能为空")
        private String metrics;
        /**
         * 查看类型（完成率、占比）
         */
        @NotBlank(message = "查看类型不能为空")
        private String viewType;
        /**
         * 搜索类型（二级部门、人员、店铺、品类、SKU）
         */
        @NotBlank(message = "搜索类型不能为空")
        private String searchType;
    }


    @Data
    @NoArgsConstructor
    public static class ViewDTO {

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
    public static class SlotDTO {
        /**
         * 销量/销售额/利润/利润率
         */
        private BigDecimal value;

        /**
         * 完成率/比例
         */
        private BigDecimal rate;
    }

}
