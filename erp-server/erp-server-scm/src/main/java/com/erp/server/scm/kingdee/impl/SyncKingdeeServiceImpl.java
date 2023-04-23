package com.erp.server.scm.kingdee.impl;

import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.server.scm.kingdee.SyncKingdeeService;
import com.erp.server.scm.service.PurchaseOrderService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
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
    private PurchaseOrderService purchaseOrderService;

    @Override
    public void updateBusinessSyncKingdeeStatus(Map<String, String> params) {
        //模块类型编码
        String code = params.get("code");
        //业务id
        String businessId = params.get("businessId");
        //更新状态
        String status = params.get("status");
        //金蝶id
        String syncKingdeeId = params.get("kingdeeId");

        //采购订单
        if (ApiModuleTypeEnum.PURCHASE_ORDER.getCode().toString().equals(code)) {
            purchaseOrderService.updateSyncKingdeeStatus(Arrays.asList(businessId),status,syncKingdeeId);
        }

    }
}
