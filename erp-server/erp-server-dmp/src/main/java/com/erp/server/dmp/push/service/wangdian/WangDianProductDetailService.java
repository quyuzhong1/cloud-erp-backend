package com.erp.server.dmp.push.service.wangdian;

import cn.wangdian.erp.sdk.api.goods.dto.GoodsBatchPushDTO;

import java.util.List;

public interface WangDianProductDetailService {
    void executeConsumer(List<GoodsBatchPushDTO> pushDTOS);
}
