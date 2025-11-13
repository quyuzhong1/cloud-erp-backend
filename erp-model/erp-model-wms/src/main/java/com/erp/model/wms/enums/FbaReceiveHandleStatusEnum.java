package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * 【海外仓入库单】
 * 交货方式类型
 *
 * @author Jim
 * @since 2024-02-19
 */
@Getter
@AllArgsConstructor
public enum FbaReceiveHandleStatusEnum implements EnumMessage {
    NONE("none", "暂无处理"),
    WAIT("wait", "待处理"),
    ALREADY("already", "已处理"),

    ;

    @EnumValue
    private final String code;
    private final String name;

    /**
     * 通过code查询
     * FbaReceiveHandleStatusEnum
     * 枚举
     */
    public static FbaReceiveHandleStatusEnum getByCode(String code) {
        return Stream.of(FbaReceiveHandleStatusEnum.values())
                .filter(e -> e.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }
}
