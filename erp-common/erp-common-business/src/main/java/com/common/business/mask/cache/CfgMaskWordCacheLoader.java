package com.common.business.mask.cache;

/**
 * 脱敏词典 Redis cache-aside 回源接口。
 *
 * <p>common 模块只定义抽象；具体 Feign 回源由 {@code erp-rpc-sys} 提供。</p>
 *
 * @author cloud-erp
 */
public interface CfgMaskWordCacheLoader {

    /**
     * 从权威数据源拉取全量未禁用词典。
     */
    CfgMaskWordFullCacheDTO load();
}
