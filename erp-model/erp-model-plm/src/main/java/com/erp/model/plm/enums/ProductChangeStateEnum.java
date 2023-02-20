package com.erp.model.plm.enums;

/**
 * @Classname ProductChangeStateEnum
 * @Description TODO
 * @Date 2023-01-28 16:30
 * @Created by yl
 */
public enum ProductChangeStateEnum {

    WAIT_AUDIT(0, "待审核"),
    AUDIT_ING(1, "审核中"),
    AUDIT_NO_PASS(2, "审核不通过"),
    AUDIT_PASS(3, "审核通过"),
    CANCELLATION(4, "已作废");

    private Integer state;
    private String name;

    ProductChangeStateEnum(Integer state, String name) {
        this.state = state;
        this.name = name;
    }

    public Integer getState() {
        return state;
    }

    public String getName() {
        return name;
    }

    public static String getName(Integer state) {
        for (BomStateEnum item : BomStateEnum.values()) {
            if (state.equals(item.getState())) {
                return item.getName();
            }
        }
        return "";
    }
}
