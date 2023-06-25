package com.erp.model.plm.dto.excel;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Lambda
 * @Classname TaskExportDTO

 * @Date 2023-06-19 14:38
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TaskExportDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class ProductTaskExcelDTO{


        private String productName;




        private String taskName;


        private LocalDate planStartTime;


        private LocalDate planEndTime;

        private String chargeId;

        private String chargeName;


        private LocalDateTime realityStartTime;


        private LocalDateTime realityEndTime;


        private String expectedDay;


        private String realityDay;

        /**
         * 工期
         */
        private Integer workPeriod;

        private Integer status;

        private String taskStatusName;

    }
}
