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
    APPROVALNAME("approvalName", "审批名称"),
    INSTANCECODE("instanceCode", "实例编号"),
    APPROVALCODE("approvalCode", "审批编号"),
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
    STATUS("status", "审批状态"),
    ENDTIME("endTime", "审批结束时间"),
    STARTTIME("startTime", "审批开始时间"),
    USERID("userId", "审批人id"),
    NODEID("nodeId", "节点id"),
    NODENAME("nodeName", "节点名称"),
    TASKLIST("taskList", "任务列表"),
    SERIALNUMBER("serialNumber", "序号"),
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
