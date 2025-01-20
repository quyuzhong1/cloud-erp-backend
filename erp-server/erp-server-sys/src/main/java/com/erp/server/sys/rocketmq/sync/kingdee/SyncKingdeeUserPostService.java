package com.erp.server.sys.rocketmq.sync.kingdee;

import java.util.Map;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.sys.entity.KingdeeUserRefPostEntity;
import com.erp.model.sys.entity.SysUserInfoEntity;

/**
 * @author Lambda
 * @Classname SyncKingdeeUserPostService
 * @Description TODO
 * @Date 2024-03-14 14:15
 * @Created by yl
 */
public interface SyncKingdeeUserPostService {

    /**
     * 同步岗位数据到金蝶
     * @description
     * @return
     * @date 2024-03-13 15:49
     * @author Lambda
     */
    DmpPushTaskEntity syncDataToKingdee(KingdeeUserRefPostEntity entity, String code);
    
    Map<String , Object> newSyncDataToKingdee(KingdeeUserRefPostEntity entity, String operate);

}
