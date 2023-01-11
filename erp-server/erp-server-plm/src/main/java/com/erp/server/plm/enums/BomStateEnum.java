package com.erp.server.plm.enums;

/**
 * bom  状态
 *
 * @Classname
 * @Description TODO
 * @Date 2023-01-09 14:43
 * @Created by yl
 */
public enum BomStateEnum {

    WAIT_SUBMIT_AUDIT(0, "待提交审核"),
    WAIT_AUDIT(1, "待审核"),
    AUDIT_ING(2, "审核中"),
    AUDIT_NO_PASS(3, "审核不通过"),
    AUDIT_PASS(4, "已归档"),
    FREEZE(5, "已冻结"),
    SCRAP(6, "已报废");

    private Integer state;
    private String name;

    BomStateEnum(Integer state, String name) {
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
