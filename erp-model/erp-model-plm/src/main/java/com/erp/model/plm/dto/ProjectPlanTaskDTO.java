package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Cloud
 * @Date 2023-01-09 12:03
 */

public class ProjectPlanTaskDTO implements Serializable {

    @Data
    @NoArgsConstructor
    @Valid
    public static class AutoDTO {

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
        private String workPeriod;
    }



}
