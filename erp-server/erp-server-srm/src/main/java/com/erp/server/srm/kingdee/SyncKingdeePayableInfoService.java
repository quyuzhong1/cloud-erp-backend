package com.erp.server.srm.kingdee;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.srm.entity.PayableDetailEntity;
import com.erp.model.srm.entity.PayableInfoEntity;

import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @version 1.0

 * @date 2023/5/23 16:24
 */
public interface SyncKingdeePayableInfoService {

    /**
     * 生成或查询任务
     * @author will
     * @date 2025/4/22 16:53
     * @param entity
     * @param detailList
     * @param operate
     * @return DmpPushTaskEntity
     */
    DmpPushTaskEntity syncDataToKingdee(PayableInfoEntity entity, List<PayableDetailEntity> detailList, String operate);
    /**
     * 获取数据
     * @author will
     * @date 2025/4/22 16:54
     * @param entity
     * @param detailList
     * @param operate
     * @return Map<String,Object>
     */
    Map<String , Object> newSyncDataToKingdee(PayableInfoEntity entity, List<PayableDetailEntity> detailList,String operate);
    /**
     * 检查是否可以反审核
     * @author will
     * @date 2025/9/25 10:29
     * @param entity
     * @return void
     */
    void checkCanDisApprove(PayableInfoEntity entity);
}
