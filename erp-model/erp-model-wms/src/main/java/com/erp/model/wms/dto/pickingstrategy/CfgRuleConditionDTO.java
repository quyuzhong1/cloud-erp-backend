package com.erp.model.wms.dto.pickingstrategy;

import com.common.business.annotation.Dict;
import com.common.business.enums.ServiceCodeNameEnum;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class CfgRuleConditionDTO {
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
        @NotBlank(message = "值不能为空")
        private String value;
        private String rightBracket;
        private String logic;
        private String name;
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
}
