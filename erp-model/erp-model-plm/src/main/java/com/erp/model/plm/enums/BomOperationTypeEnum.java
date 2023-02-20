package com.erp.model.plm.enums;

/**
 * 操作类型枚举
 *
 * @Classname BomOperationTypeEnum
 * @Description TODO
 * @Date 2023-01-09 15:22
 * @Created by yl
 */
public enum BomOperationTypeEnum {

    ADD("add", "新建"),
    DELETE("delete", "删除"),
    UPDATE("update", "编辑"),
    STATE_CHANGE("stateChange", "状态变更");


    private String type;
    private String name;

    BomOperationTypeEnum(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static String getName(String type) {
        for (BomOperationTypeEnum item : BomOperationTypeEnum.values()) {
            if (type.equals(item.getType())) {
                return item.getName();
            }
        }
        return "";
    }
}
