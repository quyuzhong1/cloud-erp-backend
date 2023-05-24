package com.erp.server.wms.kingdee.impl;

import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.server.wms.kingdee.SyncKingdeeService;
import com.erp.server.wms.service.TransferInfoService;
import com.erp.server.wms.service.WarehouseService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/10 14:43
 */
@Service
public class SyncKingdeeServiceImpl implements SyncKingdeeService {

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private TransferInfoService transferInfoService;

    @Override
    public void updateBusinessSyncKingdeeStatus(Map<String, Object> params) {
        //模块类型编码
        String code = (String)params.get("code");
        //业务id
        String businessId = (String)params.get("businessId");
        //更新状态
        String status = (String)params.get("status");
        //金蝶id
        String syncKingdeeId = (String)params.get("kingdeeId");

        //仓库
        if (ApiModuleTypeEnum.WAREHOUSE_INFO.getCode().toString().equals(code)) {
            warehouseService.updateSyncKingdeeStatus(businessId,status,syncKingdeeId);
        }
        //调拨申请单
        if (ApiModuleTypeEnum.STK_TRANSFERDIRECT.getCode().toString().equals(code)) {
            transferInfoService.updateSyncKingdeeStatus(businessId,status,syncKingdeeId,null);
        }

    }
}
