package com.erp.server.plm.rocketmq.sync.dmp;

/**
 * 同步plm产品信息
 * @Author Luo_WG
 * @Date 2023/4/19 14:02
 **/
public interface SyncPlmProductService {

    /**
     * 同步产品信息表数据到中台表
     * @Author Luo_WG
     * @Date 2023/4/19 14:03
     **/
    void syncProductInfoToDmp();

    /**
     * 产品sku表同步到中台
     * @Author Luo_WG
     * @Date 2023/4/19 14:03
     **/
    void syncProductSkuToDmp();
}
