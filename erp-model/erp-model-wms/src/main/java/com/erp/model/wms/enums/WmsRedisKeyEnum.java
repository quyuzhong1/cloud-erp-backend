package com.erp.model.wms.enums;

import org.springframework.util.Assert;

/**
 * @Classname: WmsRedisKeyEnum
 * @Description: WMS仓储服务redis的key管理
 * @CreateTime: 2023-05-06  15:36
 * @Author: zhangchunlin
 */
public enum WmsRedisKeyEnum {

    /**
     * key较多的情况：不定义具体的key, key当参数传给keyBuilder
     */
    WMS_WAREHOUSE_DETAIL_ID("WMS", "WAREHOUSE", "DETAIL", "", "仓库详情"),


    /**
     * key较少的情况：使用hash
     */

    ;

    /**
     * 系统标识
     */
    private String keyPrefix;
    /**
     * 模块名称
     */
    private String module;
    /**
     * 方法名称
     */
    private String func;
    /**
     * key
     */
    private String key;
    /**
     * 描述
     */
    private String remark;


    public String keyBuilder() {
        return toKey(key);
    }

    public String keyBuilder(String key) {
        return toKey(key);
    }

    private String toKey(String key) {
        Assert.notNull(keyPrefix, "RedisKeyEnum: keyPrefix can not be null");
        Assert.notNull(module, "RedisKeyEnum: module can not be null");
        Assert.notNull(func, "RedisKeyEnum: func can not be null");
        Assert.notNull(key, "RedisKeyEnum: key can not be null");
        return keyPrefix + ":" + module + ":" + func + ":" + key;
    }

    WmsRedisKeyEnum(String keyPrefix, String module, String func, String remark) {
        this.keyPrefix = keyPrefix;
        this.module = module;
        this.func = func;
        this.remark = remark;
    }

    WmsRedisKeyEnum(String keyPrefix, String module, String func, String key, String remark) {
        this.keyPrefix = keyPrefix;
        this.module = module;
        this.func = func;
        this.key = key;
        this.remark = remark;
    }

}
