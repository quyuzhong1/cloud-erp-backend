package com.erp.server.fms.kingdee;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.fms.entity.AssetAcceptEntity;

import java.util.Map;

public interface SyncKingdeeAssetAcceptService {
    /**
     * 推送金蝶
     */
    DmpPushTaskEntity syncDataToKingdee(AssetAcceptEntity entity, String operate);
    /**
     * 查询金蝶推送数据
     * @author will
     * @date 2025/12/30 09:49
     * @param entity
     * @param operate
     * @return Map<String,Object>
     */
    Map<String , Object> newSyncDataToKingdee(AssetAcceptEntity entity, String operate);
}
