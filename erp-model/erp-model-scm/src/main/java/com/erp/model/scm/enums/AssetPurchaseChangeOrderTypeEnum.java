package com.erp.model.scm.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @Author: wtr
 * @Date: 2025/10/27 14:22
 * @Param:
 * @Return:
 * @Description:
 **/
public enum AssetPurchaseChangeOrderTypeEnum {

    MODIFY_CHANGE("modifyChange", "修模变更"),
    CHANGE_CHANGE("changeChange","改模变更"),
    OTHER_CHANGE("otherChange","其他变更");

    private String code;
    private String name;

    AssetPurchaseChangeOrderTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }


    public static String getName(String state) {
        if (StringUtils.isNotBlank(state)) {
            for (AssetPurchaseChangeOrderTypeEnum item : values()) {
                if (state.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }
}
