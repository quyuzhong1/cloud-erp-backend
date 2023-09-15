package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

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
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class ParamDTO {

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
        /**
         * 日期
         */
        private LocalDate date;
        /**
         * 部门
         */
        private List<String> deptIdList;
        /**
         * 人员
         */
        private List<String> userIdList;
        /**
         * 店铺
         */
        private List<String> shopNameList;
        /**
         * 品类
         */
        private List<String> categoryIdList;
        /**
         * SKU
         */
        private List<String> skuNoList;
    }
}
