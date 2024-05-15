package com.erp.server.oms.kingdee;

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
    String syncDataToKingdee(SoChangeEntity entity, String operate);
}
