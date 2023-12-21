package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@ToString
@AllArgsConstructor
public enum SkuMappingRuleEnum implements EnumMessage{
    COMPLETE_SKU("completeSku","识别完整的SKU"),
    IGNORE_PREFIXES_AND_SUFFIXES("ignorePrefixesAndSuffixes","识别忽略前、后缀的SKU"),
    IGNORE_FIRST_AND_LAST_DIGITS("ignoreFirstAndLastDigits","识别忽略前、后位数的SKU"),
    EXTRACT_FIRST_TO_LAST_DIGITS("extractFirstToLast","识别截取后的SKU"),
    EXTRACT_BETWEEN_START_AND_END("extractBetweenStartAndEnd","截取SKU起始符与结束符之间的字符"),
    ;
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;

}
