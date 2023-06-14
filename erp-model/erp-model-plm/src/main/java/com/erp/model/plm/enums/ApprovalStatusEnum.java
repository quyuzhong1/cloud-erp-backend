package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;

/**
 * @Classname  立项状态
 * @Description TODO
 * @Date 2022-09-29 14:13
 * @Created by yl
 */
public enum ApprovalStatusEnum implements EnumMessage {

    WAIT(0,"未开始"),
    PROBE(1,"调研中"),
    ID_DESIGN_ING(2,"ID设计中"),
    APPROVAL(3,"已立项"),
    TERMINATE(4,"已中止"),
    SUSPEND(5,"暂停");

    private Integer code;

    private String name;


    ApprovalStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(Integer code) {
        for (ApprovalStatusEnum state : ApprovalStatusEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }
}
