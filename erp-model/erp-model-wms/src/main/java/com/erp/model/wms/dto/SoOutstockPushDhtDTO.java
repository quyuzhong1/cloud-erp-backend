package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 销售出库
 *
 * @author Lambda
 * @Classname SoOutstockDTO

 * @Date 2023-05-11 10:48
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SoOutstockPushDhtDTO implements Serializable {

    /**
     * tab list
     */
    @Data
    @NoArgsConstructor
    public static class PushDhtMainDTO {

        /**
         * 销售订单编号
         */
        private String soCode;
        /**
         * 创建时间
         */
        private String createTime;

        /**
         * 出库编号
         */
        private String code;

        /**
         * 实际发货日期
         */
        private LocalDateTime actualDeliveryDate;
    }

}
