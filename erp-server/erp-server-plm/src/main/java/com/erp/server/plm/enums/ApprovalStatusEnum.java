package com.erp.server.plm.enums;

/**
 * @Classname  立项状态
 * @Description TODO
 * @Date 2022-09-29 14:13
 * @Created by yl
 */
public enum ApprovalStatusEnum {

    WAIT(0,"未开始"),
    PROBE(1,"调研中"),
    ID_DESIGN_ING(2,"ID设计中"),
    APPROVAL(3,"已立项"),
    TERMINATE(4,"已中止");

    private Integer state;

    private String name;


    ApprovalStatusEnum(Integer colourState, String name) {
        this.state = colourState;
        this.name = name;
    }

    public Integer getState() {
        return state;
    }

    public String getName() {
        return name;
    }

    public static String getName(Integer code) {
        for (ApprovalStatusEnum state : ApprovalStatusEnum.values()) {
            if (code.equals(state.getState())) {
                return state.getName();
            }
        }
        return "";
    }
}
