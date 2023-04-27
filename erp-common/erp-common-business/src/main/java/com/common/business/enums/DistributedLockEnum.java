package com.common.business.enums;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Classname: DistributedLockEnum
 * @Description: 分布式锁key
 * @CreateTime: 2023-04-24  19:37
 * @Author: zhangchunlin
 */
public enum DistributedLockEnum {

    //系统系统
    SYS_GEN_DOCNO("SYS_GEN_DOCNO","系统服务获取单号"),


    //WMS仓库系统
    WMS_INVENTORY_SKU("WMS_INVENTORY_SKU","WMS仓储系统库存操作"),
    ;
    private String code;
    private String name;

    DistributedLockEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static DistributedLockEnum of(String code) {
        return Arrays.stream(DistributedLockEnum.values()).filter(r -> Objects.equals(code,r.getCode())).findFirst().orElse(null);
    }

    public static final String REDIS_KEY_PREFIX = "LOCK:";

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String keyBuilder(String lockKey) {
        return REDIS_KEY_PREFIX + code  + ":" + lockKey;
    }

}
