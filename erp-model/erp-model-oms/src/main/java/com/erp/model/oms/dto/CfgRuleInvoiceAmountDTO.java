package com.erp.model.oms.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 发票产品总价计算规则请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2025-07-14
*/
@Data
@NoArgsConstructor
public class CfgRuleInvoiceAmountDTO implements Serializable {




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
        * 规则名称
        */
        private String name;

        /**
        * 禁用状态 false 未禁用
        */
        private Boolean disabled;

        /**
        * 描述
        */
        private String remark;

        /**
        * 优先级
        */
        private Integer priority;

        /**
        * 发票配置id
        */
        private String cfgId;

        /**
        * 发票规则（Amount：全额，Custom：自定义，扣佣金：Deduct）
         * InvoiceRuleEnum
        */
        private String dictInvoiceRule;
        /**
         * 发票规则名称
         */
        private String dictInvoiceRuleName;

        /**
        * 比例（x100）
        */
        private BigDecimal ratio;

        /**
         * 条件
         */
        private List<RuleConditionDTO.ViewDTO> conditionList;

    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        /**
         * 条件
         */
        private List<RuleConditionDTO.ViewDTO> conditionList;
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
        /**
         * 条件
         */
        private List<RuleConditionDTO.ViewDTO> conditionList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 规则名称
        */
        @NotBlank(message = "规则名称不能为空")
        @Size(max = 50,message = "规则名称最大长度不能超过50位")
        private String name;

        /**
        * 禁用状态 false 未禁用
        */
        @NotNull(message = "禁用状态 false 未禁用不能为空")
        private Boolean disabled;

        /**
        * 描述
        */
        @NotBlank(message = "描述不能为空")
        @Size(max = 255,message = "描述最大长度不能超过255位")
        private String remark;

        /**
        * 优先级
        */
        @NotNull(message = "优先级不能为空")
        private Integer priority;

        /**
        * 发票配置id
        */
        @NotBlank(message = "发票配置id不能为空")
        @Size(max = 19,message = "发票配置id最大长度不能超过19位")
        private String cfgId;

        /**
        * 发票规则（Amount：全额，Custom：自定义，扣佣金：Deduct）
         * InvoiceRuleEnum
        */
        @NotBlank(message = "发票规则（Amount：全额，Custom：自定义，扣佣金：Deduct）不能为空")
        @Size(max = 30,message = "发票规则（Amount：全额，Custom：自定义，扣佣金：Deduct）最大长度不能超过30位")
        private String dictInvoiceRule;

        /**
        * 比例（x100）
        */
        @NotNull(message = "比例（x100）不能为空")
        @Digits(integer = 12, fraction = 4, message = "比例（x100）整数位不能超过12位，小数位不能超过4位")
        private BigDecimal ratio;


    }


}