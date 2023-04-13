package com.erp.model.workflow.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作台下拉查询选项
 * @Author Luo_WG
 * @Date 2023/4/12 12:00
 **/
public enum ApproveSearchOptionEnum {

    WAITHANDLE("waitHandle", "代办"),
    ALREADYHANDLE("alreadyHandle", "已办"),
    CARBONCOPY("carbonCopy", "抄送我"),
    INITIATE("initiate", "已发起");

    private String code;
    private String name;

    ApproveSearchOptionEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String code) {
        for (ApproveSearchOptionEnum state : ApproveSearchOptionEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }

    public static List<ApproveSearchOptionEnum> getAll() {
        List<ApproveSearchOptionEnum> resultList = new ArrayList<>();
        for (ApproveSearchOptionEnum optionEnum : ApproveSearchOptionEnum.values()) {
            resultList.add(optionEnum);
        }
        return resultList;
    }
}
