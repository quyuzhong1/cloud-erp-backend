package com.erp.server.oms.kingdee.impl;

import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.server.oms.kingdee.SyncKingdeeService;
import com.erp.server.oms.service.CustomerGroupService;
import com.erp.server.oms.service.CustomerInfoService;
import com.erp.server.oms.service.SoInfoService;
import com.erp.server.oms.service.SoReturnService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 修改操作金蝶的状态
 * @Author Luo_WG
 * @Date 2023/5/31 14:39
 **/
@Service
public class SyncKingdeeServiceImpl implements SyncKingdeeService {

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private CustomerGroupService customerGroupService;

    @Resource
    private SoInfoService soInfoService;

    @Resource
    private SoReturnService soReturnService;

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

        //客户列表
        if (ApiModuleTypeEnum.CUSTOMER_INFO.getCode().toString().equals(code)) {
            customerInfoService.updateSyncKingdeeStatus(businessId,status,syncKingdeeId, null);
        }
        //客户分组
        if (ApiModuleTypeEnum.CUSTOMER_GROUP.getCode().toString().equals(code)) {
            customerGroupService.updateSyncKingdeeStatus(businessId,status,syncKingdeeId, null);
        }
        //销售退货
        if (ApiModuleTypeEnum.SO_RETURN.getCode().toString().equals(code)) {
            soReturnService.updateSyncKingdeeStatus(businessId,status,syncKingdeeId, null);
        }
        //销售订单
        if (ApiModuleTypeEnum.SO_INFO.getCode().toString().equals(code)) {
            soInfoService.updateSyncKingdeeStatus(businessId,status,syncKingdeeId, null);
        }
        //销售变更
        if (ApiModuleTypeEnum.SO_CHANGE.getCode().toString().equals(code)) {
            soInfoService.updateSyncKingdeeStatus(businessId,status,syncKingdeeId, null);
        }
    }
}
