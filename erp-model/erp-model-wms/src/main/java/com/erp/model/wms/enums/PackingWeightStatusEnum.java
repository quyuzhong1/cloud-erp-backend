package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 装箱任务 称重状态（全部）
 */
@Getter
@AllArgsConstructor
public enum PackingWeightStatusEnum implements EnumMessage {
    UNWEIGHED("unweighed", "未称重"),
    WEIGHTING("weighing", "部分称重"),
    WEIGHTED("weighed", "全部称重"),
    SUCCESS("success", "称重成功"),
    FAIL("fail", "称重失败"),
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
            for (PackingWeightStatusEnum item : PackingWeightStatusEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static PackingWeightStatusEnum getByCode(String code) {
        PackingWeightStatusEnum[] eumnList = PackingWeightStatusEnum.values();
        for (PackingWeightStatusEnum item : eumnList) {
            if (code.equals(item.getCode())) {
                return item;
            }
        }
        return null;
    }
}
