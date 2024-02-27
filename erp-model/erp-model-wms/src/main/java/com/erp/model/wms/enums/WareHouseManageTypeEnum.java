package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

public enum WareHouseManageTypeEnum implements EnumMessage {
    NOT_PACKING("selfBuild", "自建"),
    PACKING("thirdParty", "第三方仓"),
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

    WareHouseManageTypeEnum(String code, String name) {
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
            for (WareHouseManageTypeEnum item : WareHouseManageTypeEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static WareHouseManageTypeEnum getByCode(String code) {
        WareHouseManageTypeEnum[] eumnList = WareHouseManageTypeEnum.values();
        for (WareHouseManageTypeEnum item : eumnList) {
            if (code.equals(item.getCode())) {
                return item;
            }
        }
        return null;
    }
}
