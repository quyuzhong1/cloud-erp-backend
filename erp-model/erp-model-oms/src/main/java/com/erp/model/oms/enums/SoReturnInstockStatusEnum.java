package com.erp.model.oms.enums;

import lombok.Getter;

/**
 * 销售退货单入库状态
 *
 * @author Will
 */
@Getter
public enum SoReturnInstockStatusEnum {

    /**
     * 未入库
     */
    NOT("not", "未入库"),
    /**
     * 部分入库
     */
    PARTIAL("partial", "部分入库"),
    /**
     * 已入库
     */
    INSTOCKED("instocked", "已入库"),
    /**
     * 超出退货
     */
    BEYOND("beyond", "超出退货");

    private final String code;
    private final String name;

    SoReturnInstockStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (SoReturnInstockStatusEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item.getName();
            }
        }
        return "";
    }
}
