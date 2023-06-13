package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Will
 * @version 1.0
 * @description: 出库类型
 * @date 2023/5/15 18:12
 */
public enum OutstockTypeEnum implements EnumMessage {

    SAMPLE_COLLECTION("sampleCollection", "样品领用"),
    KOL_DELIVERY("KOLDelivery", "KOL寄送"),
    AUXILIARY("auxiliary", "辅料包材"),
    ACCESSORY("accessory", "配件"),
    SCRAP("scrap", "报废");

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

    OutstockTypeEnum(String code, String name) {
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

    public static String getByCode(String code) {

        OutstockTypeEnum[] enumList = OutstockTypeEnum.values();
        for (OutstockTypeEnum item : enumList) {
            if (item.getCode().equals(code)) {
                return item.getName();
            }
        }
        return "";
    }
}
