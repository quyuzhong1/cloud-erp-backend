package com.erp.model.oms.enums;

import com.common.core.constant.EnumMessage;
import org.apache.commons.lang3.StringUtils;

/**
 * 发票状态
 */
public enum NfeCfopEnum implements EnumMessage {
    PURCHASE_SALE_SAME_CFOP("5102","采购经销-同州CFOP"),
    PURCHASE_SALE_DIFF_CFOP("6108","采购经销-跨州CFOP"),
    SELF_SALE_SAME_CFOP("5199","自产自销-同州CFOP"),
    SELF_SALE_DIFF_CFOP("6100","自产自销-跨州CFOP"),
    ;

    /**
     * 类型
     */
    private String code;
    /**
     * 名称
     */
    private String name;

    NfeCfopEnum(String code, String name) {
        this.code=code;
        this.name=name;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (NfeCfopEnum statusEnum : NfeCfopEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
