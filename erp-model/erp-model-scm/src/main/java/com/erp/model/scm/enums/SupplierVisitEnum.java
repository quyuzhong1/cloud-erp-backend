package com.erp.model.scm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * 供应商的拜访类型枚举
 * @author Lambda
 * @Classname SupplierVisitEnum

 * @Date 2023-03-21 10:09
 * @Created by yl
 */
public enum SupplierVisitEnum implements EnumMessage {

    NEW_PRODUCT("newProduct","新品"),
    ACCESS("access","准入"),
    OTHER("other","其它");


    @EnumValue
    private String code;
    private String name;

    SupplierVisitEnum(String type, String name) {
        this.code = type;
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

    public static SupplierVisitEnum getByStatus(String type){
        return Arrays.stream(values()).filter(a -> a.getCode().equals(type))
                .findFirst().orElse(null);
    }

    public static String getName(String state) {
        if (StringUtils.isNotBlank(state)) {
            for (SupplierVisitEnum item : SupplierVisitEnum.values()) {
                if (state.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static String getType(String name) {
        if (StringUtils.isNotBlank(name)) {
            for (SupplierVisitEnum item : SupplierVisitEnum.values()) {
                if (name.equals(item.getName())) {
                    return item.getCode();
                }
            }
        }
        return "";
    }

}
