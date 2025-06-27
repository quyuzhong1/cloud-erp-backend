package com.erp.model.scm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * 供应商拜访结果枚举
 *
 * @author Lambda
 * @Classname SupplierVisitResultEnumj

 * @Date 2023-03-21 10:35
 * @Created by yl
 */
public enum SupplierVisitResultEnum implements EnumMessage {

    CONFORMITY("conformity", "合格"),
    NONCONFORMITY("nonconformity", "不合格"),
    PENDING("pending", "待定");

    @EnumValue
    private String code;
    private String name;

    SupplierVisitResultEnum(String code, String name) {
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

    public static SupplierVisitResultEnum getByStatus(String code) {
        return Arrays.stream(values()).filter(a -> a.getCode().equals(code))
                .findFirst().orElse(null);
    }

    public static String getName(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (SupplierVisitResultEnum item : SupplierVisitResultEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static String getCode(String name) {
        if (StringUtils.isNotBlank(name)) {
            for (SupplierVisitResultEnum item : SupplierVisitResultEnum.values()) {
                if (name.equals(item.getName())) {
                    return item.getCode();
                }
            }
        }
        return "";
    }
}
