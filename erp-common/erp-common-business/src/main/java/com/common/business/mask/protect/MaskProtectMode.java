package com.common.business.mask.protect;

/**
 * 脱敏值回显保护模式。
 *
 * @author cloud-erp
 */
public enum MaskProtectMode {

    /**
     * 确认提交值是本次查询返回的脱敏值时，从 Redis 恢复原值后继续走原业务保存流程。
     */
    RESTORE_ORIGINAL,

    /**
     * 确认提交值是脱敏值时直接拒绝保存。
     */
    REJECT
}
