package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/22 10:48
 */
public enum ThreeGenerationPlanningEnum implements EnumMessage {

    DEVELOP_GENERATION("develop", "开发一代",""),
    RESERVE_GENERATION("reserve", "储备一代",""),
    STUDY_GENERATION("study", "研究一代","");


    private String code;

    private String name;

    private String desc;

    ThreeGenerationPlanningEnum(String code, String name,String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }
    public String getDesc() {
        return desc;
    }

    public static ThreeGenerationPlanningEnum getNameByCode(String code) {
        ThreeGenerationPlanningEnum[] enums = values();
        for (ThreeGenerationPlanningEnum plmEnum : enums) {
            if (plmEnum.getCode().equals(code)) {
                return plmEnum;
            }
        }
        return null;
    }
    public static ThreeGenerationPlanningEnum getByName(String name) {
        ThreeGenerationPlanningEnum[] enums = values();
        for (ThreeGenerationPlanningEnum plmEnum : enums) {
            if (plmEnum.getName().equals(name)) {
                return plmEnum;
            }
        }
        return null;
    }
}
