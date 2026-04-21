package com.erp.server.dmp.push.service.wdt;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.WdtCompareInventoryDTO;
import com.sdk.wangdian.sdk.api.virtualWarehouse.dto.VwPushHandelDetailPushDTO;

public interface WangDianInventoryCompareService {
    ApiResult<?> executeConsumer(WdtCompareInventoryDTO pushDTOS);
}
