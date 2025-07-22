package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zdy
 * @version 1.0
 * @description: 头程对账单类型 （logistics 物流对账单，warehouse 仓储对账单，custom 自定义物流商）
 * @date 2024/8/19 16:39
 */
public enum FirstReconciliationTypeEnum implements EnumMessage {

    LOGISTICS("logistics","物流对账单"),
    WAREHOUSE("warehouse","仓储对账单"),
    CUSTOM("custom","自定义物流商")
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

    FirstReconciliationTypeEnum(String code, String name){
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }


    public static FirstReconciliationTypeEnum getEnumByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (FirstReconciliationTypeEnum typeEnums : FirstReconciliationTypeEnum.values()) {
            if (code.equals(typeEnums.getCode())) {
                return typeEnums;
            }
        }
        return null;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (FirstReconciliationTypeEnum typeEnums : FirstReconciliationTypeEnum.values()) {
            if (code.equals(typeEnums.getCode())) {
                return typeEnums.getName();
            }
        }
        return "";
    }
}
