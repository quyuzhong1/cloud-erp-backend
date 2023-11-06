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
 * 自发货费用请求响应实体
 * </p>
 *
 * @author Will
 * @since 2023-11-06
*/
@Data
@NoArgsConstructor
public class LogisticsBillCostDTO implements Serializable {




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
        * 对账状态（字典reconciliationStatus）
        */
        private String reconciliationStatus;

        /**
        * 物流渠道id
        */
        private String channelId;

        /**
        * 物流单id
        */
        private String logisticsBillId;

        /**
        * 实重
        */
        private Integer actualWeight;

        /**
        * 体积重
        */
        private Integer volumeWeight;

        /**
        * 计费重
        */
        private Integer billingWeight;

        /**
        * 预估运费
        */
        private BigDecimal estimatedShippingCost ;

        /**
        * 计费重（物流商）
        */
        private Integer billingWeightLogistics;

        /**
        * 实际运费（物流商）
        */
        private BigDecimal lactualShippingCost;

        /**
        * 运费差异
        */
        private BigDecimal diffShippingCost;

        /**
        * 币别
        */
        private String currency;

        /**
        * 备注
        */
        private String remark;


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
        * 对账状态（字典reconciliationStatus）
        */
        @NotBlank(message = "对账状态（字典reconciliationStatus）不能为空")
        @Size(max = 32,message = "对账状态（字典reconciliationStatus）最大长度不能超过32位")
        private String reconciliationStatus;

        /**
        * 物流渠道id
        */
        @NotBlank(message = "物流渠道id不能为空")
        @Size(max = 19,message = "物流渠道id最大长度不能超过19位")
        private String channelId;

        /**
        * 物流单id
        */
        @NotBlank(message = "物流单id不能为空")
        @Size(max = 19,message = "物流单id最大长度不能超过19位")
        private String logisticsBillId;

        /**
        * 实重
        */
        @NotNull(message = "实重不能为空")
        private Integer actualWeight;

        /**
        * 体积重
        */
        @NotNull(message = "体积重不能为空")
        private Integer volumeWeight;

        /**
        * 计费重
        */
        @NotNull(message = "计费重不能为空")
        private Integer billingWeight;

        /**
        * 预估运费
        */
        @NotNull(message = "预估运费不能为空")
        @Digits(integer = 12, fraction = 4, message = "预估运费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal estimatedShippingCost ;

        /**
        * 计费重（物流商）
        */
        @NotNull(message = "计费重（物流商）不能为空")
        private Integer billingWeightLogistics;

        /**
        * 实际运费（物流商）
        */
        @NotNull(message = "实际运费（物流商）不能为空")
        @Digits(integer = 12, fraction = 4, message = "实际运费（物流商）整数位不能超过12位，小数位不能超过4位")
        private BigDecimal lactualShippingCost;

        /**
        * 运费差异
        */
        @NotNull(message = "运费差异不能为空")
        @Digits(integer = 12, fraction = 4, message = "运费差异整数位不能超过12位，小数位不能超过4位")
        private BigDecimal diffShippingCost;

        /**
        * 币别
        */
        @NotBlank(message = "币别不能为空")
        @Size(max = 32,message = "币别最大长度不能超过32位")
        private String currency;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;


    }


}