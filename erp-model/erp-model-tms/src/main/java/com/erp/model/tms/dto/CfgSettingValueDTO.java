package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 系统配置管理请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-01-08
*/
@Data
@NoArgsConstructor
public class CfgSettingValueDTO implements Serializable {
    @Data
    @NoArgsConstructor
    public static class LogisticsProductDestDeclarePrice{
        /**
         * 含税采购价（>）
         */
        @Digits(integer = 12, fraction = 4, message = "含税采购价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal startPrice;
        /**
         * 含税采购价(<=)
         */
        @Digits(integer = 12, fraction = 4, message = "含税采购价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal endPrice;
        /**
         * 比例
         */
        @Digits(integer = 12, fraction = 2, message = "比例整数位不能超过12位，小数位不能超过2位")
        private BigDecimal rate;
    }

}