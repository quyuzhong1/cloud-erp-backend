package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DeclareNatureLevyEnum implements EnumMessage {
    COMMONLY("commonly","一般征税"),
    VEHICLE_TAXATION("vehicleTaxation","整车征税"),
    TAXATION("taxation","零部件征税"),
    GRATUITOUS_ASSISTANCE("gratuitousAssistance","无偿援助"),
    OTHER_STATUTORY("otherStatutory","其他法定"),
    SPECIFIC_AREAS("specificAreas","特定区域"),
    BONDED_AREA("bondedArea","保税区"),
    OTHER_AREA("otherArea","其他地区"),
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


    DeclareNatureLevyEnum(String code, String name) {
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
}
