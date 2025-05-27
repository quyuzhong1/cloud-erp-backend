package com.erp.model.workflow.enums;

/**
 * @description:
 * @author: hcg
 * @date: 2025/5/16 20:09
 */

/**
 *@Author: hcg
 *@CreateTime: 2025-05-16
 *@Description:
 *@Version: 1.0
 */
public enum FsRequestBodyAttributesEnum {
    APPROVAL_NAME("approval_name", "审批名称"),
    FORM("form", "控件参数信息"),
    NAME("name", "控件名"),
    CHILDREN("children", "fieldList的子控件集合信息"),
    TEXT("text", "选项值"),
    VALUE("value", "值"),
    OPTION("option", "选项列表"),
    FIELDLIST("fieldList", "明细控件类型"),
    ID("id", "控件唯一标识"),
    REQUIRED("required", "是否必填"),
    CURRENCY("currency", "币种"),
    CURRENCYRange("currencyRange", "币种"),
    TYPE("type", "审批终止");

    private final String code;
    private final String description;

    FsRequestBodyAttributesEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
