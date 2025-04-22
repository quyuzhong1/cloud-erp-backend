package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author zdy
 * @ClassName FullyManagedDTO
 * @description: 全托管
 * @date 2025年03月25日
 * @version: 1.0
 */
@Data
@NoArgsConstructor
public class FullyManagedDTO implements Serializable {
    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class WarningDTO{
        /**
         * 销售订单
         */
        private  String id;
        /**
         * 销售订单编号
         */
        private  String code;
        /**
         * 平台编码
         */
        private  String platformCode;
        /**
         * 发货预警时间
         */
        private LocalDateTime deliveryWarningTime;
        /**
         * 要求发货时间
         */
        private LocalDateTime requiredDeliveryTime;
    }
}
