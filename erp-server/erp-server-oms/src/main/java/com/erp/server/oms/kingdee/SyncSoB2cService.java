package com.erp.server.oms.kingdee;

import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoInfoEntity;

import java.util.List;
import java.util.Map;

public interface SyncSoB2cService {

    /**
     * 同步订单到数帝云的字段映射处理
     * @param soB2cEntity
     * @param soB2cDetailEntity
     * @param operate
     * @return
     */
    Map<String, Object> syncDataToSdyFieldHandler(SoB2cEntity soB2cEntity, SoB2cDetailEntity soB2cDetailEntity, String operate);

    void syncDataToSdy(SoB2cEntity soB2cEntity, SoB2cDetailEntity soB2cDetailEntity, String operate);
}
