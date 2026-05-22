package com.erp.model.tms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 费用类型配置枚举
 * @author will
 * @date 2026/1/28 18:09
 */
public enum CfgLogisticsCostImportBusinessTypeEnum implements EnumMessage {
    LAST_MILE_LOGISTICS_BILL_COST("lastMileLogisticsBillCost", "尾程费用(三方发货)"),
    LOGISTICS_BILL_COST("logisticsBillCost", "尾程费用(自发货)"),
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

    CfgLogisticsCostImportBusinessTypeEnum(String code, String name) {
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
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (CfgLogisticsCostImportBusinessTypeEnum statusEnum : CfgLogisticsCostImportBusinessTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }

    public static String getCode(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        }
        for (CfgLogisticsCostImportBusinessTypeEnum statusEnum : CfgLogisticsCostImportBusinessTypeEnum.values()) {
            if (name.equals(statusEnum.getName())) {
                return statusEnum.getCode();
            }
        }
        return "";
    }
}
