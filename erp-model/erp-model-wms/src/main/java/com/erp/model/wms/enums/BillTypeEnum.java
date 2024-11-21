package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 单据类型
 */
public enum BillTypeEnum implements EnumMessage {
    PROFIT("profit", "盘盈单", "profitLoss"),
    LOSS("loss", "盘亏单", "profitLoss"),
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
     * 类型
     */
    private String type;


    BillTypeEnum(String code, String name, String type) {
        this.code = code;
        this.name = name;
        this.type = type;
    }


    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (BillTypeEnum billTypeEnum : BillTypeEnum.values()) {
            if (code.equals(billTypeEnum.getCode())) {
                return billTypeEnum.getName();
            }
        }
        return "";
    }


    public static List<BillTypeEnum> listByType(String type) {
        List<BillTypeEnum> list = new ArrayList<>();
        for (BillTypeEnum billTypeEnum : BillTypeEnum.values()) {
            if (type.equals(billTypeEnum.getType())) {
                list.add(billTypeEnum);
            }
        }
        return list;
    }
}
