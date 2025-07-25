package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 导入公共类型
 * @author will
 * @date 2025/7/22 20:17
 */
public enum ImportCommonTypeEnum implements EnumMessage  {
    UPDATE_ALL("updateAll", "更新全部"),
    UPDATE_PART("updatePart", "更新部分");

    @EnumValue
    @JsonValue
    private String code;
    private String name;

    ImportCommonTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }


    @Override
    public String getCode() {
        return code;
    }
    @Override
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (ImportCommonTypeEnum item : ImportCommonTypeEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static ImportCommonTypeEnum getByCode(String code){
        return Arrays.stream(values()).filter(a -> a.getCode().equalsIgnoreCase(code))
                .findFirst().orElse(null);
    }

    public static List<String> getCodeList() {
        return Arrays.stream(ImportCommonTypeEnum.values()).map(ImportCommonTypeEnum::getCode).collect(Collectors.toList());
    }
}
