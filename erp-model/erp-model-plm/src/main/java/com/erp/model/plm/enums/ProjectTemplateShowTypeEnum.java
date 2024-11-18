package com.erp.model.plm.enums;

/**
 * @author Will
 * @version 1.0
 * @description: 模板类型枚举
 * @date 2022/11/11 15:13
 */
public enum ProjectTemplateShowTypeEnum {

    APPROVAL_TEMPLATE(1, "立项模板"),
    PROJECT_DEFAULT_TEMPLATE(2, "项目模板【默认】"),
    PROJECT_CUSTOM_TEMPLATE(3, "项目模板");

    private Integer code;
    private String name;

    ProjectTemplateShowTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public static String getNameByCode(Integer code) {
        ProjectTemplateShowTypeEnum[] projectTemplateTypeEnums = values();
        for (ProjectTemplateShowTypeEnum projectTemplateTypeEnum : projectTemplateTypeEnums) {
            if (projectTemplateTypeEnum.getCode().equals(code)) {
                return projectTemplateTypeEnum.getName();
            }
        }
        return null;
    }

    public static ProjectTemplateShowTypeEnum getEnumByType(Integer code){
        ProjectTemplateShowTypeEnum[] projectTemplateTypeEnums = values();
        for (ProjectTemplateShowTypeEnum projectTemplateTypeEnum : projectTemplateTypeEnums) {
            if (projectTemplateTypeEnum.getCode().equals(code)) {
                return projectTemplateTypeEnum;
            }
        }
        return null;
    }

    public static Integer getCodeByName(String name) {
        ProjectTemplateShowTypeEnum[] projectTemplateTypeEnums = values();
        for (ProjectTemplateShowTypeEnum projectTemplateTypeEnum : projectTemplateTypeEnums) {
            if (projectTemplateTypeEnum.getName().equals(name)) {
                return projectTemplateTypeEnum.getCode();
            }
        }
        return null;
    }
}
