package com.erp.model.tms.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 配置核对类型
 */
@Getter
@AllArgsConstructor
public enum CfgReconciliationTypeEnum implements EnumMessage {
    B2C_DECLARE("b2cDeclare", "B2C报关对账单", DictCostAttributionEnum.DECLARE, DictBasicEnum.CFG_B2C_DECLARE_ERP_FIELD),
    FIRST_MILE("firstMile", "头程对账单", DictCostAttributionEnum.FIRST_MILE, DictBasicEnum.CFG_FIRST_MILE_ERP_FIELD),
    ;
    private final String code;
    private final String name;
    private final DictCostAttributionEnum costAttributionEnum;

    /**
     * 字典配置类型
     */
    private final DictBasicEnum dictBasicEnum;

    public static CfgReconciliationTypeEnum getByDictBasicEnum(String dictBasicEnum) {
        return Arrays.stream(CfgReconciliationTypeEnum.values())
                .filter(e -> e.getDictBasicEnum().getType().equalsIgnoreCase(dictBasicEnum))
                .findFirst()
                .orElse(null);
    }

    public static CfgReconciliationTypeEnum getByCode(String code) {
        return Arrays.stream(CfgReconciliationTypeEnum.values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    public static String getNameByCode(String code) {
        if (null == code) {
            return "";
        }
        CfgReconciliationTypeEnum customsTypeNewEnum = Arrays.stream(CfgReconciliationTypeEnum.values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
        if (null == customsTypeNewEnum) {
            return "";
        }
        return customsTypeNewEnum.getName();
    }

    public static boolean checkSupplier(CfgReconciliationTypeEnum typeEnum) {
        return CfgReconciliationTypeEnum.B2C_DECLARE.equals(typeEnum) || CfgReconciliationTypeEnum.FIRST_MILE.equals(typeEnum);
    }
}