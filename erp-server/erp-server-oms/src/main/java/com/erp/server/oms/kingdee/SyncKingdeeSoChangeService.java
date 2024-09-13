package com.erp.server.oms.kingdee;

import java.util.Map;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.oms.entity.SoChangeEntity;

/**
 * @author Lambda
 * @Classname SyncKingdeeSoService

 * @Date 2023-05-30 11:46
 * @Created by yl
 */
public interface SyncKingdeeSoChangeService {

    /**
     * 推送金蝶
     */
    DmpPushTaskEntity syncDataToKingdee(SoChangeEntity entity, String operate);
    
    Map<String, Object> newSyncDataToKingdee(SoChangeEntity entity, String operate);
}
