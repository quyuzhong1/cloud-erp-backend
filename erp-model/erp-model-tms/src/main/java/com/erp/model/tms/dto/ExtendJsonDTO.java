package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Will
 * @version 1.0
 * @description: 其他费用数值设置DTO
 * @date 2023/11/6 14:55
 */
@Data
public class ExtendJsonDTO {

    private ExtendJsonDTO() {
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO implements Serializable {

        /**
         * 最长边
         */
        @Digits(integer = 12, fraction = 4, message = "最长边值整数位不能超过12位，小数位不能超过4位")
        private BigDecimal longestEdge;

        /**
         * 次长边
         */
        @Digits(integer = 12, fraction = 4, message = "次长边值整数位不能超过12位，小数位不能超过4位")
        private BigDecimal minorEdge;

        /**
         * 三边和
         */
        @Digits(integer = 12, fraction = 4, message = "三边和值整数位不能超过12位，小数位不能超过4位")
        private BigDecimal edgelSum;

        /**
         * 任意一边
         */
        @Digits(integer = 12, fraction = 4, message = "任意一边值整数位不能超过12位，小数位不能超过4位")
        private BigDecimal anyEdge;
    }
}
