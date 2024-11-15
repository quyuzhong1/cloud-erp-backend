package com.erp.model.wms.dto.pickingstrategy;

import com.common.business.annotation.Dict;
import com.common.business.enums.ServiceCodeNameEnum;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class CfgRuleConditionDTO {
    private CfgRuleConditionDTO() {
        throw new IllegalStateException("Utility CfgRuleConditionDTO class");
    }
    @Getter
    @Setter
    public static class View {
        private String id;
        private String leftBracket;
        @Dict(tableName = "cfg_condition", queryFieldName = "condition_field", returnFieldName = "condition_field_name")
        private String field;
        @Dict(serviceCode = ServiceCodeNameEnum.OMS, tableName = "dict_rule_condition", queryFieldName = "key", returnFieldName = "value")
        private String compare;
        private String value;
        private String rightBracket;
        @Dict(serviceCode = ServiceCodeNameEnum.OMS, tableName = "dict_rule_condition", queryFieldName = "key", returnFieldName = "value")
        private String logic;
        private String name;
        private Integer index;

    }

    @Getter
    @Setter
    public static class Common {
        private String leftBracket;
        @NotBlank(message = "条件的字段不能为空")
        private String field;
        @NotBlank(message = "比较符不能为空")
        private String compare;
        private String value;
        private String rightBracket;
        private String logic;
        private String name;
        /**
         * 字段名称
         */
        private String fieldName;
    }

    @Getter
    @Setter
    public static class Add extends Common {

    }

    @Getter
    @Setter
    public static class Update extends Common {
        private String id;
    }

    @Getter
    @Setter
    public static class ConditionElementDTO {
        /**
         * id
         */
        private String id;
        /**
         * 左括号
         */
        private String leftBracket;

        /**
         * 条件的字段
         */
        private String field;

        /**
         * 比较符
         */
        private String compare;

        /**
         * 对应的值
         */
        private String value;

        /**
         * 右括号
         */
        private String rightBracket;

        /**
         * 逻辑关系 or 和 and
         */
        private String logic;

        /**
         * 规则id
         */
        private String ruleId;

        /**
         * 顺序
         */
        private Integer index;

        /**
         * 值对应名称
         */
        private String name;
        /**
         * 所属类型
         */
        private String sourceType;

        /**
         * 对应的值类型
         */
        private String valueType;
    }
}
