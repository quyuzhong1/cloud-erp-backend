package com.erp.model.scm.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @Author: wtr
 * @Date: 2026/1/14 17:23
 * @Param:
 * @Return:
 * @Description:
 **/
public enum SubcontractOrderTypeEnum {

    COMMON_SUBCONTRACT("commonSubcontract", "普通委外订单"),
    REPAIR_SUBCONTRACT("repairSubcontract","返修委外订单");


    private String code;
    private String name;

    SubcontractOrderTypeEnum(String code, String name) {
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
            for (SubcontractOrderTypeEnum item : values()) {
                if (state.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }
}
