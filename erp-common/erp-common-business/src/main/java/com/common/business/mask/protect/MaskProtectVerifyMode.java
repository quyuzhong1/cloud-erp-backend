package com.common.business.mask.protect;

/**
 * 脱敏回显保护安全校验方式。
 *
 * @author cloud-erp
 */
public enum MaskProtectVerifyMode {

    /**
     * 保存时在同一事务内锁读数据库当前值，并要求当前值与 Redis 中的查询时原值一致。
     */
    DB_VALUE_COMPARE,

    /**
     * 保存 DTO 自带 version / updateTime 时，用读写两侧版本字段校验。
     */
    PARAM_VERSION,

    /**
     * 不做自动恢复；提交脱敏占位时拒绝。
     */
    REJECT
}
