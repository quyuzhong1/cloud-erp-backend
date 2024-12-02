package com.erp.server.oms.kingdee;

import java.util.Map;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.SoDetailEntity;
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

    Map<String, Object> newSyncDataToKingdee(SoInfoEntity entity, String operate);

    /**
     * 同步数帝云字段映射处理
     */
    Map<String, Object> syncDataToSdyFieldHandler(SoInfoDTO.ViewDTO view, SoDetailEntity soDetailEntity, String operate, String deliveryStatus);

    /**
     * 同步数帝云
     * @param view
     * @param soDetailEntity
     * @param operate
     * @param deliveryStatus
     */
    void syncDataToSdy(SoInfoDTO.ViewDTO view, SoDetailEntity soDetailEntity, String operate, String deliveryStatus);
}