package com.erp.common.business.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * 定制字段
 *
 * @Classname
 * @Description TODO
 * @Date 2023-02-08 14:22
 * @Created by yl
 */


public enum CustomizeFieldEnum {
    TASK_NAME("任务名称", "taskName", true, "plmScheduleTask", "plm项目计划任务"),
    SCHEDULE_STATUS("排期审核状态", "scheduleStatusName", true, "plmScheduleTask", "plm项目计划任务"),
    TASK_CHARGE_NAME("任务负责人", "chargeName", true, "plmScheduleTask", "plm项目计划任务"),
    PLAN_START_TIME("计划开始时间", "planStartTime", true, "plmScheduleTask", "plm项目计划任务"),
    PLAN_END_TIME("计划结束时间", "planEndTime", true, "plmScheduleTask", "plm项目计划任务"),
    REALITY_START_TIME("实际开始时间", "realityStartTime", false, "plmScheduleTask", "plm项目计划任务"),
    REALITY_END_TIME("实际结束时间", "realityEndTime", false, "plmScheduleTask", "plm项目计划任务"),
    PHASE_NAME("阶段名", "phaseName", false, "plmScheduleTask", "plm项目计划任务"),
    TASK_TYPE("任务类型", "typeName", false, "plmScheduleTask", "plm项目计划任务"),
    PRODUCT_NAME("产品名称", "productName", false, "plmScheduleTask", "plm项目计划任务"),
    PRE_TASK_NAMES("前置任务", "preTaskNames", false, "plmScheduleTask", "plm项目计划任务"),
    TASK_PRIORITY("任务优先级", "priorityName", false, "plmScheduleTask", "plm项目计划任务"),
    IS_MILEPOST("设置里程碑", "isMilepostName", false, "plmScheduleTask", "plm项目计划任务"),
    REF_SKU("关联的sku", "refSkuNoList", false, "plmScheduleTask", "plm项目计划任务"),
    DELIVERY_DOCS_NAMES("交付文档", "deliveryDocsNames", false, "plmScheduleTask", "plm项目计划任务"),
    TASK_DESCRIPTION("描述", "description", false, "plmScheduleTask", "plm项目计划任务"),
    TASK_CREATE_USER_NAME("创建人", "createUserName", false, "plmScheduleTask", "plm项目计划任务"),
    TASK_CREATE_TIME("创建时间", "createTime", false, "plmScheduleTask", "plm项目计划任务"),
    TASK_UPDATE_USER_NAME("更新人", "updateUserName", false, "plmScheduleTask", "plm项目计划任务"),
    TASK_UPDATE_TIME("更新时间", "updateTime", false, "plmScheduleTask", "plm项目计划任务");


    //字段标题
    public String fieldTitle;
    //字段名称
    public String fieldName;
    //模块code
    public String moduleCode;
    //模块名称
    public String moduleName;

    //是否默认
    public boolean isDefault;


    public String getFieldTitle() {
        return fieldTitle;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public String getModuleName() {
        return moduleName;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    CustomizeFieldEnum(String fieldTitle, String fieldName, boolean isDefault, String moduleCode, String moduleName) {
        this.fieldTitle = fieldTitle;
        this.fieldName = fieldName;
        this.isDefault = isDefault;
        this.moduleCode = moduleCode;
        this.moduleName = moduleName;
    }

    public static List<CustomizeFieldEnum> getByModuleCode(String moduleCode) {
        List<CustomizeFieldEnum> resultList = new ArrayList<>(10);
        for (CustomizeFieldEnum item : CustomizeFieldEnum.values()) {
            if (moduleCode.equals(item.getModuleCode())) {
                resultList.add(item);
            }
        }
        return resultList;
    }


    public static String getByModuleFieldTitle(String fieldName) {
        for (CustomizeFieldEnum item : CustomizeFieldEnum.values()) {
            if (fieldName.equals(item.getModuleCode())) {
                item.getFieldTitle();
            }
        }
        return "";
    }
}
