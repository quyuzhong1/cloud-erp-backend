package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Cloud
 * @Date 2023-01-09 12:03
 */

public class ProjectPlanTaskDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class AutoDTo{

        /**
         *  方法入口类型 1初始化排期 2是变更排期
         */
        @NotNull(message = "入口类型不能为空")
        private Integer type;

        @NotEmpty(message = "需要排期列表不能为空")
        private List<AutoDateDTO> list;

    }



    @Data
    @NoArgsConstructor
    @Valid
    public static class AutoDateDTO {

        /**
         * 任务计划id
         */
        @NotBlank(message = "记录ID不能为空")
        private String id;
        /**
         * 任务计划开始时间
         */
        private LocalDate startDate;
        /**
         * 任务计划结束时间
         */
        private LocalDate endDate;

        /**
         * 工期
         */
        @NotBlank(message = "工期不能为空")
        private Integer workPeriod;


    }



}
