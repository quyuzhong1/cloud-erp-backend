package com.erp.model.wms.dto.pickingstrategy;

import com.common.business.annotation.Dict;
import com.common.business.enums.ServiceCodeNameEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CfgRuleConditionDTO {

    private String id;
    private String leftBracket;
    @Dict(serviceCode = ServiceCodeNameEnum.OMS, tableName = "dict_rule_condition", queryFieldName = "key", returnFieldName = "value")
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
