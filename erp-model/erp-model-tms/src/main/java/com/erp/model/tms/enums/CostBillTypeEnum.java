package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 单据类型
 */
public enum CostBillTypeEnum implements EnumMessage {
    B2B("B2B", "B2B订单"),
    B2C("B2C", "B2C订单"),
    AFTER_SALES("AfterSale","售后订单-补/换/赠"),
    SORETURN_INSTOCK("soReturnInstock", "退货入库单"),
    OTHER("other", "其他"),
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


    CostBillTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (CostBillTypeEnum billTypeEnum : CostBillTypeEnum.values()) {
            if (code.equals(billTypeEnum.getCode())) {
                return billTypeEnum.getName();
            }
        }
        return "";
    }

    public static String getCodeByName(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        }
        for (CostBillTypeEnum billTypeEnum : CostBillTypeEnum.values()) {
            if (name.trim().equals(billTypeEnum.getName())) {
                return billTypeEnum.getCode();
            }
        }
        return "";
    }
}
