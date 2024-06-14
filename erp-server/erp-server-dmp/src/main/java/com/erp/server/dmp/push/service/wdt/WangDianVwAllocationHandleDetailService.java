package com.erp.server.dmp.push.service.wdt;

import com.sdk.wangdian.sdk.api.goods.dto.VwAllocationHandelDetailPushDTO;

public interface WangDianVwAllocationHandleDetailService {
    void executeConsumer(VwAllocationHandelDetailPushDTO pushDTOS);
}
