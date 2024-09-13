package com.erp.server.oms.kingdee;

import java.util.Map;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.oms.entity.SoInfoEntity;

/**
 * @author Lambda
 * @Classname SyncKingdeeSoService

 * @Date 2023-05-30 11:46
 * @Created by yl
 */
public interface SyncKingdeeSoService {

    /**
     * 推送金蝶
     */
    DmpPushTaskEntity syncDataToKingdee(SoInfoEntity entity, String operate);
    void syncOrderToDmp(SoInfoEntity entity, String operate);
    
    Map<String , Object> newSyncDataToKingdee(SoInfoEntity entity, String operate);
}
