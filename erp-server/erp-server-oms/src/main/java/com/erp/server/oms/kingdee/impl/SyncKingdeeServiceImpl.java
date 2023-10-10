package com.erp.server.oms.kingdee.impl;

import com.common.business.dto.base.PushSyncStatusDTO;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.server.oms.kingdee.SyncKingdeeService;
import com.erp.server.oms.service.*;
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
    private SoChangeService soChangeService;

    @Resource
    private SoReturnService soReturnService;

    @Resource
    private CustomerContactService customerContactService;

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

        PushSyncStatusDTO.KingdeeDTO syncKingdeeDTO = new PushSyncStatusDTO.KingdeeDTO(businessId,"",syncKingdeeId, status);


        //客户列表
        if (ApiModuleTypeEnum.CUSTOMER_INFO.getCode().toString().equals(code)) {
            customerInfoService.updateSyncKingdeeStatus(syncKingdeeDTO);
        }
        //客户分组
        if (ApiModuleTypeEnum.CUSTOMER_GROUP.getCode().toString().equals(code)) {
            customerGroupService.updateSyncKingdeeStatus(syncKingdeeDTO);
        }
        //销售订单
        if (ApiModuleTypeEnum.SO_INFO.getCode().toString().equals(code)) {
            soInfoService.updateSyncKingdeeStatus(syncKingdeeDTO);
        }
        //销售变更
        if (ApiModuleTypeEnum.SO_CHANGE.getCode().toString().equals(code)) {
            soChangeService.updateSyncKingdeeStatus(syncKingdeeDTO);
        }
        //客户联系人
        if (ApiModuleTypeEnum.CUSTOMER_CONTACT.getCode().toString().equals(code)) {
            customerContactService.updateSyncKingdeeStatus(syncKingdeeDTO);
        }
    }
}
