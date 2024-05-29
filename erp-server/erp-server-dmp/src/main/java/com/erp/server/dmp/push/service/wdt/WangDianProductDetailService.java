package com.erp.server.dmp.push.service.wdt;

import com.sdk.wangdian.sdk.api.goods.dto.GoodsBatchPushDTO;

public interface WangDianProductDetailService {
    void executeConsumer(GoodsBatchPushDTO pushDTOS);
}
