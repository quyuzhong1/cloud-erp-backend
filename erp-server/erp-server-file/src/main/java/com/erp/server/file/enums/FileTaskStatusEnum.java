package com.erp.server.file.enums;

public enum FileTaskStatusEnum {
    /**
     * 等待中
     */
    PENDING,
    /**
     * 处理中
     */
    PROCESS,
    /**
     * 全部成功
     */
    FINISH,
    /**
     * 部分成功
     */
    PART,
    /**
     * 全部失败
     */
    FAIL,
    /**
     * 已失效
     */
    EXPIRED
}