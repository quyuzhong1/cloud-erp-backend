package com.erp.model.tms.enums;


import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * <p>
 * 小包费用分摊 大表状态 枚举
 * </p>
 *
 * @author shukai
 * @since 2024-12-02 10:48:45
 */
public enum WeightAllocationStatusEnum implements EnumMessage {
	TODO("toDo", "待生成"),
	DONE("done", "已生成"),
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

    WeightAllocationStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (CharSequenceUtil.isBlank(code)) {
            return "";
        }
        for (WeightAllocationStatusEnum statusEnum : WeightAllocationStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
