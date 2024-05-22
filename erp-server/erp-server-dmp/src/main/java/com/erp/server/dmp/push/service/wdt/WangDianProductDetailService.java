package com.erp.server.dmp.push.service.wdt;

import com.sdk.wangdian.sdk.api.goods.dto.GoodsBatchPushDTO;

import java.util.List;

public interface WangDianProductDetailService {
    void executeConsumer(List<GoodsBatchPushDTO> pushDTOS);
}
