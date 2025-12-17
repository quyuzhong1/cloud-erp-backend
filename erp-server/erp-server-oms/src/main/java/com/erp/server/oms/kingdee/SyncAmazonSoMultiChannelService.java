package com.erp.server.oms.kingdee;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.oms.entity.SoMultiChannelEntity;

import java.util.Map;

/**
 * @description: 亚马逊订单多渠道同步
 * @author zdy
 * @Classname SyncAmazonSoMultiChannelService
 * @Date 2023-05-30 11:46
 * @Created by zdy
 */
public interface SyncAmazonSoMultiChannelService {

    /**
     * 推送金蝶
     */
    DmpPushTaskEntity syncDataToKingdee(SoMultiChannelEntity entity, String operate);
    
    Map<String, Object> newSyncDataToKingdee(SoMultiChannelEntity entity, String operate);
}
