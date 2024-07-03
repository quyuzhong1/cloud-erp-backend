package com.erp.server.dmp.push.service.wdt;

import com.sdk.wangdian.sdk.api.virtualWarehouse.dto.VwPushHandelDetailPushDTO;

public interface WangDianVwPushHandleDetailService {
    void executeConsumer(VwPushHandelDetailPushDTO pushDTOS);
}
