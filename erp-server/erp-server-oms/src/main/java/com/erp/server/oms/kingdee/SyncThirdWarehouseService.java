package com.erp.server.oms.kingdee;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.oms.dto.ThirdWarehouseCreateOutboundPushDTO;

import java.util.Map;

/**
 * @description: 亚马逊订单多渠道同步
 * @author zdy
 * @Classname SyncAmazonSoMultiChannelService
 * @Date 2023-05-30 11:46
 * @Created by zdy
 */
public interface SyncThirdWarehouseService {

    /**
     * 推送金蝶
     */
    DmpPushTaskEntity syncDataToThirdWarehouse(ThirdWarehouseCreateOutboundPushDTO entity, String operate);
    
    Map<String, Object> newSyncDataToThirdWarehouse(ThirdWarehouseCreateOutboundPushDTO entity, String operate);
}
