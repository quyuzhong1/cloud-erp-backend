package com.erp.model.scm.enums;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/27 10:52
 */
public enum PurchaseOrderConfirmTypeEnum {

    //全部
    ALL("all", "全部"),
    //待确认
    TO_BE_CONFIRM("toBeConfirm", "待确认"),
    //已确认
    CONFIRM("confirm", "已确认"),
    //已拒绝
    REJECT("reject", "已拒绝"),
    //送货中
    DELIVERY("delivery", "送货中"),
    // 已完成
    FINISH("finish", "已完成"),
    //已关闭
    CLOSED("closed",	"已关闭");


    private String code;
    private String name;

    PurchaseOrderConfirmTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }


    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        PurchaseOrderConfirmTypeEnum[] stateEnums = values();
        for (PurchaseOrderConfirmTypeEnum stateEnum : stateEnums) {
            if (stateEnum.getCode().equals(code) ) {
                return stateEnum.getName();
            }
        }
        return "";
    }
}
