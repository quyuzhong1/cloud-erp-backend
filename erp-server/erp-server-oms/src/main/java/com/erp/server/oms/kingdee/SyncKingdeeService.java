package com.erp.server.oms.kingdee;

import java.util.Map;

/**
 * 修改操作金蝶的状态
 * @Author Luo_WG
 * @Date 2023/5/31 14:40
 **/
public interface SyncKingdeeService {

    /**
     * 参数，code类型编码，businessId业务id，status状态，kingdeeId金蝶id
     */
    void updateBusinessSyncKingdeeStatus(Map<String, Object> params);
}
