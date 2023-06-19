package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Lambda
 * @Classname ProductTask
 * @Description TODO
 * @Date 2023-06-19 9:50
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductTask  implements Serializable {


    @Data
    @NoArgsConstructor
    public static class  TaskInfoDTO{



        /**
         * 任务id
         */
        private String taskId;


        /**
         * 任务名
         */
        private String name;

        /**
         * 阶段id
         */
        private String  phaseId;

        /**
         * 阶段名
         */
        private String  phaseName;

        /**
         * 产品id
         */
        private String  productId;

        /**
         * 负责人id
         */
        private String chargeId;

        /**
         * 负责人id集合
         */
        private List<String> chargeIdList;

        /**
         * 负责人名
         */
        private String chargeName;

        /**
         * 任务状态
         */
        private Integer status;

        /**
         * 任务状态名
         */
        private String statusName;


        /**
         * 计划开始时间
         */
        private LocalDate planStartTime;

        /**
         * 计划结束时间
         */
        private LocalDate planEndTime;

        /**
         * 延期天数
         */
        private Integer delayDays;
    }



}
