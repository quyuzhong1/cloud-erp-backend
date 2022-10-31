package com.common.core.enums;

/**
 * @Classname 流程审核状态
 * @Description TODO
 * @Date 2022-08-15 11:12
 * @Created by yl
 */
public enum ApproveStateCodeEnum {

    UN_COMMIT(1, "待提交"),
    APPROVING(2, "审批中"),
    APPROVE_REJECT(3, "审批不通过"),
    APPROVE_PASS(4, "审批通过");


    private Integer code;
    private String name;

    ApproveStateCodeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }
    public String getName() {
        return name;
    }
}
