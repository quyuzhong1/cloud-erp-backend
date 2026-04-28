package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang.StringUtils;

/**
 * @Author: wtr
 * @Date: 2026/3/13 15:55
 * @Param:
 * @Return:
 * @Description:
 **/
public enum WarsehouseOperationDescriptionEnum implements EnumMessage {

    DROP_DOWN("dropDown", "下拉框"),
    INPUT("input", "输入框"),
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

    WarsehouseOperationDescriptionEnum(String code, String name) {
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
            for (WarsehouseOperationDescriptionEnum item : WarsehouseOperationDescriptionEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static WarsehouseOperationDescriptionEnum getByCode(String code) {
        WarsehouseOperationDescriptionEnum[] eumnList = WarsehouseOperationDescriptionEnum.values();
        for (WarsehouseOperationDescriptionEnum item : eumnList) {
            if (code.equals(item.getCode())) {
                return item;
            }
        }
        return null;
    }
}
