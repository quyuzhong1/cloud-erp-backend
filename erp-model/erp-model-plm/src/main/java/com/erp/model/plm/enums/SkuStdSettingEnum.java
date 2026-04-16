package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;
import com.common.core.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 分摊配置
 */
@Getter
@AllArgsConstructor
public enum SkuStdSettingEnum implements EnumMessage {
    RETAIL_STD("retail_std", "标准零售价"),
//    COST_FIN("cost_fin", "财务导入成本"),
    // 采购平均成本
    COST_AVG("cost_avg", "中台SKU成本")
    ;
    private final String code;
    private final String name;


    public static SkuStdSettingEnum getByCode(String code) {
        return Arrays.stream(SkuStdSettingEnum.values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new ServiceException("SKU标准导入类型不存在: " + code));
    }

    public static String getNameByCode(String code) {
        if (null == code) {
            return "";
        }
        SkuStdSettingEnum customsTypeNewEnum = Arrays.stream(SkuStdSettingEnum.values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
        if (null == customsTypeNewEnum) {
            return "";
        }
        return customsTypeNewEnum.getName();
    }
}