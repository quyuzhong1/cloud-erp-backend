package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * SKU标准导入类型
 */
@Getter
@AllArgsConstructor
public enum SkuStdCostImportTypeEnum implements EnumMessage {
    CHANGE("change", "导入变更"),
    UPDATE("update", "导入更新")
    ;
    private final String code;
    private final String name;


    public static SkuStdCostImportTypeEnum getByCode(String code) {
        return Arrays.stream(SkuStdCostImportTypeEnum.values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    public static String getNameByCode(String code) {
        if (null == code) {
            return "";
        }
        SkuStdCostImportTypeEnum customsTypeNewEnum = Arrays.stream(SkuStdCostImportTypeEnum.values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
        if (null == customsTypeNewEnum) {
            return "";
        }
        return customsTypeNewEnum.getName();
    }
}