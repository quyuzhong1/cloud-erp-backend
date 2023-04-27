package com.erp.server.plm.rocketmq.sync.kingdee.impl;

import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeService;
import com.erp.server.plm.service.BasicCategoryService;
import com.erp.server.plm.service.BomInfoService;
import com.erp.server.plm.service.ProductDetailService;
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
    private ProductDetailService productDetailService;

    @Resource
    private BomInfoService bomInfoService;

    @Resource
    private BasicCategoryService basicCategoryService;

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

        //产品管理
        if (ApiModuleTypeEnum.PRODUCT_DETAIL.getCode().toString().equals(code)) {
            productDetailService.updateSyncKingdeeStatus(businessId,status,syncKingdeeId);
        }
        //bom管理
        if (ApiModuleTypeEnum.BOM_INFO.getCode().toString().equals(code)) {
            bomInfoService.updateSyncKingdeeStatus(businessId,status,syncKingdeeId);
        }
        //产品管理
        if (ApiModuleTypeEnum.ONE_LEVEL_CATEGORY.getCode().toString().equals(code) || ApiModuleTypeEnum.SECOND_LEVEL_CATEGORY.getCode().toString().equals(code)) {
            basicCategoryService.updateSyncKingdeeStatus(businessId,status,syncKingdeeId);
        }
    }
}
