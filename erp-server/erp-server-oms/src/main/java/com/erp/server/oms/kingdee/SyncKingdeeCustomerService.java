package com.erp.server.oms.kingdee;

import cn.hutool.core.lang.Pair;
import com.erp.model.oms.entity.CustomerInfoEntity;

import java.util.List;

public interface SyncKingdeeCustomerService {
    /**
     * 推送金蝶
     */
    Pair<String, List<String>> syncDataToKingdee(CustomerInfoEntity entity, String operate);
}
