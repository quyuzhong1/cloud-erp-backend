package com.erp.model.mrp.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class CfgRuleOverseasInstockDaysDTO {


    @Setter
    @Getter
    public static class UpdateDTO {
        /**
         * 主键id
         */
        private String id;

        @NotBlank(message = "仓库id不能为空")
        private String warehouseId;

        /**
         * 入库天数（天）
         */
        @NotNull(message = "海外仓入库天数（天）不能为空")
        @Min(value = 0, message = "海外仓入库天数（天）最小值为0")
        @Max(value = 365, message = "海外仓入库天数（天）最大值为365")
        private Integer instockDays;
    }

    @Setter
    @Getter
    public static class ViewDTO {

        private String warehouseId;

        /**
         * 入库天数（天）
         */
        private Integer instockDays;
    }
}
