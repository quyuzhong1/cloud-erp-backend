package com.erp.server.plm.enums;

/**
 * @author Will
 * @version 1.0
 * @description: 模板类型枚举
 * @date 2022/11/11 15:13
 */
public enum ProjectTemplateTypeEnum {

    APPROVAL_TEMPLATE(1, "立项模板"),
    PROJECT_TEMPLATE(2, "项目模板");

    private Integer code;
    private String name;

    ProjectTemplateTypeEnum(Integer code, String name) {
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
        ProjectTemplateTypeEnum[] projectTemplateTypeEnums = values();
        for (ProjectTemplateTypeEnum projectTemplateTypeEnum : projectTemplateTypeEnums) {
            if (projectTemplateTypeEnum.getCode() == code) {
                return projectTemplateTypeEnum.getName();
            }
        }
        return null;
    }

    public static ProjectTemplateTypeEnum getEnumByType(String code){
        ProjectTemplateTypeEnum[] projectTemplateTypeEnums = values();
        for (ProjectTemplateTypeEnum projectTemplateTypeEnum : projectTemplateTypeEnums) {
            if (projectTemplateTypeEnum.getCode().equals(code)) {
                return projectTemplateTypeEnum;
            }
        }
        return null;
    }

    public static Integer getCodeByName(String name) {
        ProjectTemplateTypeEnum[] projectTemplateTypeEnums = values();
        for (ProjectTemplateTypeEnum projectTemplateTypeEnum : projectTemplateTypeEnums) {
            if (projectTemplateTypeEnum.getName().equals(name)) {
                return projectTemplateTypeEnum.getCode();
            }
        }
        return null;
    }
}
