package com.erp.model.srm.enums;

/**
 * 应付单类型
 * @author will
 * @date 2025/9/29 15:04
 */
public enum PayableTypeEnum {

    PURCHASE_INSTOCK("purchaseInstock", "标准采购入库"),
    SUBCONTRACT_INSTOCK("subcontractInstock", "标准委外入库"),
    PURCHASE_RETURN("purchaseReturn","标准采购退货"),
    SUBCONTRACT_RETURN("subcontractReturn","标准委外退货")
    ;

    private String code;
    private String name;

    PayableTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        PayableTypeEnum[] stateEnums = values();
        for (PayableTypeEnum stateEnum : stateEnums) {
            if (stateEnum.getCode().equals(code) ) {
                return stateEnum.getName();
            }
        }
        return "";
    }
    public static PayableTypeEnum getEnum(String code) {
        for (PayableTypeEnum settingEnum : PayableTypeEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum;
            }
        }
        return null;
    }
}
