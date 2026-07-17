package com.common.business.mask.cache;

/**
 * 字段脱敏配置 Redis cache-aside 回源接口。
 *
 * <p>{@code erp-common-business} 不直接依赖 sys Feign；业务服务引入
 * {@code erp-rpc-sys} 后由 Feign 实现回源。</p>
 *
 * @author cloud-erp
 */
public interface CfgMaskFieldCacheLoader {

    /**
     * 从权威数据源拉取全量未禁用字段规则。
     */
    CfgMaskFieldFullCacheDTO load();
}
