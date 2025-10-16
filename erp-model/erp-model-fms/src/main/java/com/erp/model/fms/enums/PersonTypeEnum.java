package com.erp.model.fms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 验收人员类型枚举
 * @author wuht
 * @since 2025-10-13
 */
@Getter
@AllArgsConstructor
public enum PersonTypeEnum {
    
    /**
     * 采购开发人员
     */
    PURCHASE_DEV("purchaseDev", "采购开发"),
    
    /**
     * 质量工程师
     */
    QUALITY_ENGINEER("qualityEngineer", "质量工程师"),
    
    /**
     * 结构工程师
     */
    STRUCTURE_ENGINEER("structureEngineer", "结构工程师"),
    
    /**
     * 产品经理
     */
    PRODUCT_MANAGER("productManager", "产品经理"),
    
    /**
     * 项目经理
     */
    PROJECT_MANAGER("projectManager", "项目经理");
    
    /**
     * 人员类型编码
     */
    private final String code;
    
    /**
     * 人员类型名称
     */
    private final String name;
    
    /**
     * 根据编码获取枚举
     * @param code 人员类型编码
     * @return 枚举
     */
    public static PersonTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (PersonTypeEnum personType : values()) {
            if (personType.getCode().equals(code)) {
                return personType;
            }
        }
        return null;
    }
    
    /**
     * 根据编码获取名称
     * @param code 人员类型编码
     * @return 人员类型名称
     */
    public static String getName(String code) {
        PersonTypeEnum personType = getByCode(code);
        return personType != null ? personType.getName() : code;
    }
    
    /**
     * 获取所有编码列表
     * @return 编码列表
     */
    public static String[] getCodeList() {
        return new String[]{
            PURCHASE_DEV.getCode(),
            QUALITY_ENGINEER.getCode(),
            STRUCTURE_ENGINEER.getCode(),
            PRODUCT_MANAGER.getCode(),
            PROJECT_MANAGER.getCode()
        };
    }
    
    /**
     * 获取所有名称列表
     * @return 名称列表
     */
    public static String[] getNameList() {
        return new String[]{
            PURCHASE_DEV.getName(),
            QUALITY_ENGINEER.getName(),
            STRUCTURE_ENGINEER.getName(),
            PRODUCT_MANAGER.getName(),
            PROJECT_MANAGER.getName()
        };
    }
}
