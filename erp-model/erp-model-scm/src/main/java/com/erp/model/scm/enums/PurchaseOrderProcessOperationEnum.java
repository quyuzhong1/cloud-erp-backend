package com.erp.model.scm.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/30 15:11
 */
public enum PurchaseOrderProcessOperationEnum {

    CREATE("create", "创建"),
    APPROVE("approve", "审核"),
    RECEIVE("receive", "签收"),
    FINISH_RECEIVE("finishReceive", "签收完成");


    private String code;
    private String name;

    PurchaseOrderProcessOperationEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }
}
