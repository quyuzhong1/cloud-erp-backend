package com.erp.server.wms.kingdee;

import java.util.Map;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/10 14:43
 */
public interface SyncKingdeeService {

    /**
     * 参数，code类型编码，businessId业务id，status状态，kingdeeId金蝶id
     */
    void updateBusinessSyncKingdeeStatus(Map<String, Object> params);
}
