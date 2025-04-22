package com.erp.server.wms.kingdee;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.wms.entity.TransferInDetailEntity;
import com.erp.model.wms.entity.TransferInEntity;

import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @version 1.0

 * @date 2023/5/23 16:24
 */
public interface SyncKingdeeTransferInService {

    /**
     * 生成或查询任务
     * @author will
     * @date 2025/4/22 16:53
     * @param entity
     * @param detailList
     * @param operate
     * @return DmpPushTaskEntity
     */
    DmpPushTaskEntity syncDataToKingdee(TransferInEntity entity, List<TransferInDetailEntity> detailList, String operate);
    /**
     * 获取数据
     * @author will
     * @date 2025/4/22 16:54
     * @param entity
     * @param detailList
     * @param operate
     * @return Map<String,Object>
     */
    Map<String , Object> newSyncDataToKingdee(TransferInEntity entity, List<TransferInDetailEntity> detailList,String operate);
}
