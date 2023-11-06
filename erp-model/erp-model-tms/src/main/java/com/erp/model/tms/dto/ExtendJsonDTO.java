package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Will
 * @version 1.0
 * @description: 其他费用数值设置DTO
 * @date 2023/11/6 14:55
 */
@Data
@NoArgsConstructor
public class ExtendJsonDTO {

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 最长边
         */
        private BigDecimal longestEdge;

        /**
         * 次长边
         */
        private BigDecimal minorEdge;

        /**
         * 三边和
         */
        private BigDecimal edgelSum;

        /**
         * 任意一边
         */
        private BigDecimal anyEdge;
    }
}
