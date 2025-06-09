package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 分货单方向
 * @author hyj
 * @version 1.0

 */
public enum VwAllocationDirectionEnum implements EnumMessage {


    FORWARD (1, "正向"),
    REVERSE(-1, "反向");

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private Integer code;
    /**
     * 名称
     */
    private String name;

    VwAllocationDirectionEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (VwAllocationDirectionEnum item : VwAllocationDirectionEnum.values()) {
                if (Objects.equals(code,item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }
}
