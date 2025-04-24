package com.erp.server.tms.service;

import java.util.Map;

/**
 * @author zdy
 * @ClassName AsyncService
 * @date 2023年12月14日
 * @version: 1.0
 */
public interface AsyncService {

    /**
     * 异步拉取销售渠道数据
     */
    void asyncUpdateSaleChannel(Map<String, String> authMap);

    /**
     * 异步拉取中转物流商渠道数据
     */
    void asyncUpdateTransferLogisticsChannel(String logisticsPlatform, String authId, String mainId);
}
