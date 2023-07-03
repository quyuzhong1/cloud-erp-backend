package com.erp.server.wms.mabang;

import com.erp.model.wms.entity.MachineInfoEntity;

/**
 * @Classname: SyncMabangMachineService
 * @Description: 加工单推送到马帮生成出入库
 * @CreateTime: 2023-07-02  11:21
 * @Author: zhangchunlin
 */
public interface SyncMabangMachineService {

    /**
     * 直接调拨单推送到马帮
     */
    void syncDataToMabang(MachineInfoEntity entity, String operate);

}
