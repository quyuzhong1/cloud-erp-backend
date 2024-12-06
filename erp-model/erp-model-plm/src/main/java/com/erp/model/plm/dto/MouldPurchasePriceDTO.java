package com.erp.model.plm.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 模具价目表请求响应实体
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
*/
@Data
@NoArgsConstructor
public class MouldPurchasePriceDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 模具id
        */
        private String mouldDetailId;

        /**
        * 数量
        */
        private Integer qty;

        /**
        * 含税单价
        */
        private BigDecimal taxPrice;

        /**
        * 税率
        */
        private BigDecimal taxRate;

        /**
        * 结算方式
        */
        private String payMethodId;

        /**
        * 付款条件
        */
        private String paymentCondition;

        /**
        * 币种
        */
        private String currency;

        /**
        * 汇率
        */
        private BigDecimal exchangeRate;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 模具id
        */
        @NotBlank(message = "模具id不能为空")
        @Size(max = 19,message = "模具id最大长度不能超过19位")
        private String mouldDetailId;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;

        /**
        * 含税单价
        */
        @NotNull(message = "含税单价不能为空")
        @Digits(integer = 12, fraction = 4, message = "含税单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal taxPrice;

        /**
        * 税率
        */
        @NotNull(message = "税率不能为空")
        @Digits(integer = 12, fraction = 4, message = "税率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal taxRate;

        /**
        * 结算方式
        */
        @NotBlank(message = "结算方式不能为空")
        @Size(max = 19,message = "结算方式最大长度不能超过19位")
        private String payMethodId;

        /**
        * 付款条件
        */
        @NotBlank(message = "付款条件不能为空")
        @Size(max = 255,message = "付款条件最大长度不能超过255位")
        private String paymentCondition;

        /**
        * 币种
        */
        @NotBlank(message = "币种不能为空")
        @Size(max = 255,message = "币种最大长度不能超过255位")
        private String currency;

        /**
        * 汇率
        */
        @NotNull(message = "汇率不能为空")
        @Digits(integer = 12, fraction = 4, message = "汇率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal exchangeRate;


    }


}