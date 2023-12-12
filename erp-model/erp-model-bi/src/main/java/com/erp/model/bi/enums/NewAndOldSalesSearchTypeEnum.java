package com.erp.model.bi.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 新老品销售额搜索类型枚举
 * @Author Luo_WG
 * @Date 2023/9/15 11:56
 **/
public enum NewAndOldSalesSearchTypeEnum implements EnumMessage {
    DIVISION("division","事业部"),
    USER("user","人员"),
    SHOP("shop","店铺"),
    CATEGORY("category","品类"),
    ;

    NewAndOldSalesSearchTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    public String code;
    /**
     * 名称
     */
    private String name;


    @Override
    public Object getCode() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    public static String getNameByCode(String code) {
        NewAndOldSalesSearchTypeEnum[] enums = values();
        for (NewAndOldSalesSearchTypeEnum typeEnum : enums) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum.getName();
            }
        }
        return null;
    }

    public static NewAndOldSalesSearchTypeEnum getEnumByCode(String code) {
        NewAndOldSalesSearchTypeEnum[] enums = values();
        for (NewAndOldSalesSearchTypeEnum typeEnum : enums) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
