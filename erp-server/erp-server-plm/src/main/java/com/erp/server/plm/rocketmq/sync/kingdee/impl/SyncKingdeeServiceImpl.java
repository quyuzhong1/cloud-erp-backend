package com.erp.server.plm.rocketmq.sync.kingdee.impl;

import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeService;
import com.erp.server.plm.service.BasicCategoryService;
import com.erp.server.plm.service.ProductBomHistoryService;
import com.erp.server.plm.service.ProductDetailService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/10 14:43
 */
@Service
public class SyncKingdeeServiceImpl implements SyncKingdeeService {

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private ProductBomHistoryService productBomHistoryService;

    @Resource
    private BasicCategoryService basicCategoryService;

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

        //产品管理
        if (ApiModuleTypeEnum.PRODUCT_DETAIL.getCode().toString().equals(code)) {
            productDetailService.updateSyncKingdeeId(businessId,syncKingdeeId);
        }
        //bom管理
        if (ApiModuleTypeEnum.BOM_INFO.getCode().toString().equals(code)) {
            productBomHistoryService.updateSyncKingdeeStatus(businessId,status,syncKingdeeId);
        }
        //产品管理
        if (ApiModuleTypeEnum.ONE_LEVEL_CATEGORY.getCode().toString().equals(code) || ApiModuleTypeEnum.SECOND_LEVEL_CATEGORY.getCode().toString().equals(code)) {
            basicCategoryService.updateSyncKingdeeId(businessId,syncKingdeeId);
        }
    }
}
