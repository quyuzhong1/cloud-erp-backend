package com.common.message.enums;

/**
 * @author Will
 * @version 1.0
 * @description: 辅助资料枚举
 * @date 2023/4/6 10:59
 */
public enum AssistantDataEnum {

    ONE_LEVEL_CATEGORY("oneLevelCategory", "一级分类"),
    SECOND_LEVEL_CATEGORY("secondLevelCategory", "二级分类"),
    ;
    private String code;

    private String name;


    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    AssistantDataEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
