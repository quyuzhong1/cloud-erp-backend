package com.erp.model.scm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class PurchaseStatisticsDTO implements Serializable {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RequestDTO {

        /**
         * 供应商Id
         */
        @NotBlank(message = "供应商Id不能为空")
        private String supplierId;

        /**
         * 执行状态
         */
        private String executionStatus;

        /**
         * 开始时间
         */
        private LocalDateTime startTime;

        /**
         * 结束时间
         */
        private LocalDateTime endTime;
    }

    @Data
    @NoArgsConstructor
    public static class ResponseDTO {
        /**
         * 按月份统计
         */
        private List<StatisticsMonthDTO> statisticsMonthDTOList;
    }

    @Data
    @NoArgsConstructor
    public static class StatusDTO {
        private Integer count;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatisticsMonthDTO {

        /**
         * 月份
         */
        private Integer month;

        /**
         * 订单量
         */
        private Integer orderCount;

        /**
         * 订单SKU量
         */
        private Integer orderSkuCount;

        /**
         * 订单额
         */
        private BigDecimal orderMoney;

        public static StatisticsMonthDTO buildZeroData(Integer month){
            return StatisticsMonthDTO.builder()
                    .month(month)
                    .orderCount(0)
                    .orderSkuCount(0)
                    .orderMoney(BigDecimal.ZERO)
                    .build();
        }
    }
}
