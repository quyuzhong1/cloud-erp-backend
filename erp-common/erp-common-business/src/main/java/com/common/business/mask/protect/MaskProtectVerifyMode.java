package com.common.business.mask.protect;

/**
 * 脱敏回显保护安全校验方式。
 *
 * @author cloud-erp
 */
public enum MaskProtectVerifyMode {

    /**
     * 保存时在同一事务内锁读数据库当前值，作为无权限字段的恢复值。
     */
    DB_VALUE_COMPARE,

    /**
     * 保留兼容枚举；去掉读侧原值缓存后不再用于自动恢复。
     */
    PARAM_VERSION,

    /**
     * 不做自动恢复；无权限写入时拒绝。
     */
    REJECT
}
