package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: wtr
 * @Date: 2026/3/17 8:50
 * @Param:
 * @Return:
 * @Description: 仓库操作类型值枚举
 **/
public enum WarehouseOperationTypeValueEnum implements EnumMessage {
    //zhongbao
    TRUE("true","1", "是"),
    FALSE("false", "-1","否"),

    NOT("not","-1", "无"),
    OPEN_BOX_CHANGE("openBoxChange", "2","开箱换"),
    NOT_OPERN_BOX_CHANGE("notOpenBoxChange", "3","不开箱换"),

    SINGLE("single","2", "单面"),
    DOUBLE("double","3", "双面"),
    FOUR("four","4", "四面"),
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

    /**
     * 名称
     */
    private String value;



    WarehouseOperationTypeValueEnum(String code,String value, String name) {
        this.code = code;
        this.value = value;
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

    public String getValue() {
        return value;
    }


    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (WarehouseOperationTypeValueEnum billTypeEnum : WarehouseOperationTypeValueEnum.values()) {
            if (code.equals(billTypeEnum.getCode())) {
                return billTypeEnum.getName();
            }
        }
        return "";
    }

    public static String getValueByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (WarehouseOperationTypeValueEnum enumItem : WarehouseOperationTypeValueEnum.values()) {
            if (code.equals(enumItem.getCode())) {
                return enumItem.getValue();
            }
        }
        return "";
    }

}