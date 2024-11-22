package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zdy
 * @ClassName InspectionEnum
 * @description: 验货状态
 * @date 2024年04月18日
 * @version: 1.0
 */
public enum InspectionEnum implements EnumMessage {
    YES("true", "已验货", "b2cDeliveryOrder"),
    NO("false", "未验货", "b2cDeliveryOrder"),
            ;
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;

    /**
     * 类型
     */
    private String type;


    InspectionEnum(String code, String name, String type) {
        this.code = code;
        this.name = name;
        this.type = type;
    }


    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (InspectionEnum inspectionEnum : InspectionEnum.values()) {
            if (code.equals(inspectionEnum.getCode())) {
                return inspectionEnum.getName();
            }
        }
        return "";
    }


    public static List<InspectionEnum> listByType(String type) {
        List<InspectionEnum> list = new ArrayList<>();
        for (InspectionEnum inspectionEnum : InspectionEnum.values()) {
            if (type.equals(inspectionEnum.getType())) {
                list.add(inspectionEnum);
            }
        }
        return list;
    }
}
