package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * @Author: wtr
 * @Date: 2026/3/25 9:05
 * @Param:
 * @Return:
 * @Description:
 **/
public enum QcStandardImageTypeEnum {

    PRODUCT_PHYSICAL ("productPhysical", "产品实物"),
    PACKAGING_ACCESSORIES("packagingAccessories", "包装配件");

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

    QcStandardImageTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    public static String getByCode(String code) {
        QcStandardImageTypeEnum qcStandardImageTypeEnum  = Arrays.stream(values()).filter(p -> p.getCode().equals(code))
                .findFirst().orElse(null);
        if (qcStandardImageTypeEnum != null) {
            return qcStandardImageTypeEnum.getName();
        }
        return "";
    }
}
