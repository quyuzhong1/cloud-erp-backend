package com.erp.model.wms.dto;

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
public class PurchaseReturnStatisticsDTO implements Serializable {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RequestDTO {

        /**
         * 供应商Id
         */
        @NotBlank(message = "供应商Id不能为空")
        private String surpplierId;

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
    @AllArgsConstructor
    @Builder
    public static class StatisticsMonthDTO {

        /**
         * 月份
         */
        private Integer month;

        /**
         * 退货订单数量
         */
        private Integer returnCount;

        /**
         * 退货SKU量
         */
        private Integer returnSkuCount;

        /**
         * sku质检退货量
         */
        private Integer qcReturnSkuCount;

        /**
         * 退货总额
         */
        private BigDecimal returnMoney;

        public static StatisticsMonthDTO buildZeroData(Integer month){
            return StatisticsMonthDTO.builder()
                    .month(month)
                    .returnCount(0)
                    .returnSkuCount(0)
                    .qcReturnSkuCount(0)
                    .returnMoney(BigDecimal.ZERO)
                    .build();
        }
    }
}
