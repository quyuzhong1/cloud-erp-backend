package com.erp.server.oms.rocketmq.sync.wangdian;


import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.oms.dto.KolSubB2cApplicationDTO;
import com.erp.model.plm.vo.SkuVO;
import com.sdk.wangdian.sdk.api.sales.dto.PushSelf2Request;

import java.util.Map;

public interface SyncWangDianSoB2cService {

    DmpPushTaskEntity syncDataToWangDian(KolSubB2cApplicationDTO.PushDTO pushDTO, Map<String, SkuVO> skuMap );

    DmpPushTaskEntity syncCancelDataToWangDian(KolSubB2cApplicationDTO.PushDTO pushDTO, Map<String, SkuVO> skuMap );

    PushSelf2Request newSyncKolB2c(KolSubB2cApplicationDTO.PushDTO pushDTO, Map<String, SkuVO> skuMap);

    PushSelf2Request newSyncKolB2c(KolSubB2cApplicationDTO.PushDTO pushDTO, Map<String, SkuVO> skuMap, String syncOperate);
}
