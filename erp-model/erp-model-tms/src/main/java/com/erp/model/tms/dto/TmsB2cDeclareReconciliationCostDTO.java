package com.erp.model.tms.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * B2c报关单请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-03-26
*/
@Data
@NoArgsConstructor
public class TmsB2cDeclareReconciliationCostDTO implements Serializable {




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
        * 主表id（对账单明细id）
        */
        private String mainId;

        /**
        * 费用值
        */
        private BigDecimal costValue;

        /**
        * 币别
        */
        private String currency;

        /**
        * 汇率
        */
        private BigDecimal exchangeRate;

        /**
        * 费用设置id
        */
        private String cfgCostId;

        /**
        * 类型（estimated预估、actual实际）
        */
        private String type;


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
        * 主表id（对账单明细id）
        */
        @NotBlank(message = "主表id（对账单明细id）不能为空")
        private String mainId;

        /**
        * 费用值
        */
        @NotNull(message = "费用值不能为空")
        @Digits(integer = 12, fraction = 4, message = "费用值整数位不能超过12位，小数位不能超过4位")
        private BigDecimal costValue;

        /**
        * 币别
        */
        @NotBlank(message = "币别不能为空")
        @Size(max = 32,message = "币别最大长度不能超过32位")
        private String currency;

        /**
        * 汇率
        */
        @NotNull(message = "汇率不能为空")
        @Digits(integer = 12, fraction = 4, message = "汇率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal exchangeRate;

        /**
        * 费用设置id
        */
        @NotBlank(message = "费用设置id不能为空")
        @Size(max = 19,message = "费用设置id最大长度不能超过19位")
        private String cfgCostId;

        /**
        * 类型（estimated预估、actual实际）
        */
        @NotBlank(message = "类型（estimated预估、actual实际）不能为空")
        @Size(max = 32,message = "类型（estimated预估、actual实际）最大长度不能超过32位")
        private String type;


    }


}