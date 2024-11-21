package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 装箱任务 装箱状态
 */
@Getter
@AllArgsConstructor
public enum PackingTaskStatusEnum implements EnumMessage {
    WAIT("wait","未生成"),
    UNPACKED("unpacked", "待装箱"),
    PACKING("packing", "装箱中"),
    PACKED("packed", "已装箱"),
    INCOMPLETE("incomplete", "未完成"),
    COMPLETED("completed", "已完成"),
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

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (PackingTaskStatusEnum item : PackingTaskStatusEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static PackingTaskStatusEnum getByCode(String code) {
        PackingTaskStatusEnum[] eumnList = PackingTaskStatusEnum.values();
        for (PackingTaskStatusEnum item : eumnList) {
            if (code.equals(item.getCode())) {
                return item;
            }
        }
        return null;
    }
}
