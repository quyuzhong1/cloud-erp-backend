package com.erp.server.file.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FileTaskStatusEnum implements EnumMessage {
    /**
     * 待处理
     */
    PENDING("PENDING", "待处理"),
    /**
     * 处理中
     */
    PROCESS("PROCESS", "处理中"),
    /**
     * 已完成
     */
    FINISH("FINISH", "已完成"),
    /**
     * 处理失败
     */
    FAIL("FAIL", "处理失败"),
    /**
     * 手动终止
     */
    STOP("STOP", "手动终止");

    private final String code;
    private final String name;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }
}