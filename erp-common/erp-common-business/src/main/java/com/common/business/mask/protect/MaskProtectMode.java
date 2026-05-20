package com.common.business.mask.protect;

/**
 * 脱敏值回显保护模式。
 *
 * @author cloud-erp
 */
public enum MaskProtectMode {

    /**
     * 无字段权限时，锁读数据库当前值并覆盖入参字段后继续走原业务保存流程。
     */
    RESTORE_ORIGINAL,

    /**
     * 无字段权限时，把保存 DTO 字段置为 null 后继续走原业务保存流程。
     */
    SET_NULL,

    /**
     * 无字段权限时直接拒绝保存。
     */
    REJECT
}
