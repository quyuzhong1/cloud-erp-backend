package com.erp.model.wms.enums;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * 出库通知状态枚举
 * @author will
 * @date 2025/8/29 17:52
 */
@Getter
@AllArgsConstructor
public enum IsAllowOutstockEnum implements EnumMessage {
    WAIT_NOTICE(Boolean.FALSE,"待通知出库"),
    PERMIT(Boolean.TRUE,"允许出库"),
    ;

    @EnumValue
    private final Boolean code;
    private final String name;

    /**
     * 通过code查询
     * OverseasFinishStatus
     * 枚举名称
     */
    public static String getNameByCode(Boolean code) {
        if (ObjectUtil.isNull(code)) {
            return "";
        }
        IsAllowOutstockEnum resultEnum = getByCode(code);
        return null == resultEnum ? "" : resultEnum.getName();
    }

    /**
     * 通过code查询
     * OverseasFinishStatus
     * 枚举
     */
    public static IsAllowOutstockEnum getByCode(Boolean code) {
        return Stream.of(IsAllowOutstockEnum.values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
