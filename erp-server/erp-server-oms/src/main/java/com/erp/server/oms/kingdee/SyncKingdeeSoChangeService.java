package com.erp.server.oms.kingdee;

import com.erp.model.oms.entity.SoInfoEntity;

/**
 * @author Lambda
 * @Classname SyncKingdeeSoService
 * @Description TODO
 * @Date 2023-05-30 11:46
 * @Created by yl
 */
public interface SyncKingdeeSoChangeService {

    /**
     * 推送金蝶
     */
    void syncDataToKingdee(SoInfoEntity entity, String operate);
}
