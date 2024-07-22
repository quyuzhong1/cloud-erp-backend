package com.erp.server.wms.service.impl;

import com.common.business.validator.ValidGroup;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.wms.dto.inventory.*;
import com.erp.model.wms.enums.inventory.InventoryBizTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.server.wms.config.InventoryHelper;
import com.erp.server.wms.service.InventoryStockService;
import com.erp.server.wms.service.InventoryTransCoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @Classname: InventoryTransCoreServiceImpl
 * @Description: 库存交易核心处理类
 * @CreateTime: 2023-05-06  09:30
 * @Author: zhangchunlin
 * Update By Edison.Qu  2023-09-28
 */
//@Service
public class InventoryTransCoreServiceImplOld implements InventoryTransCoreService {


    @Resource
    private InventoryHelper inventoryHelper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approveByType(InventoryInOutStockDTO busiParam) {
        ValidatorUtil.validateEntity(busiParam);
        InventoryStockService inventoryService = inventoryHelper.getInventoryService(InventoryBizTypeEnum.IN_OUT_STOCK);
        inventoryService.approve(busiParam.getParamList(), null, InventoryBusinessTypeEnum.getByCode(busiParam.getBusinessType()), true);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approveByType(InventoryTransferDTO busiParam) {
        ValidatorUtil.validateEntity(busiParam);
        InventoryStockService inventoryService = inventoryHelper.getInventoryService(InventoryBizTypeEnum.TRANSFER_STOCK);
        inventoryService.approve(busiParam.getParamList(), null, InventoryBusinessTypeEnum.getByCode(busiParam.getBusinessType()), true);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approveByRule(InventoryTransferRuleDTO busiParam) {
        ValidatorUtil.validateEntity(busiParam);
        InventoryStockService inventoryService = inventoryHelper.getInventoryService(InventoryBizTypeEnum.TRANSFER_STOCK);
        inventoryService.approve(busiParam.getParamList(), busiParam.getRules(), InventoryBusinessTypeEnum.getByCode(busiParam.getBusinessType()), false);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approveByRule(InventoryInOutStockRuleDTO busiParam) {
        ValidatorUtil.validateEntity(busiParam, ValidGroup.Update.class);
        InventoryStockService inventoryService = inventoryHelper.getInventoryService(InventoryBizTypeEnum.IN_OUT_STOCK);
        inventoryService.approve(busiParam.getParamList(), busiParam.getRules(), InventoryBusinessTypeEnum.getByCode(busiParam.getBusinessType()), false);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unApprove(InventoryUnApproveDTO busiParam) {
        ValidatorUtil.validateEntity(busiParam);
        InventoryStockService inventoryService = inventoryHelper.getInventoryService(InventoryBizTypeEnum.IN_OUT_STOCK);
        inventoryService.unApprove(busiParam);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchUnApprove(InventoryBatchUnApproveDTO busiParam) {
        ValidatorUtil.validateEntity(busiParam);
        busiParam.getBillIds().forEach(billId->{
            InventoryUnApproveDTO inventoryUnApproveDTO = new InventoryUnApproveDTO();
            inventoryUnApproveDTO.setSourceType(busiParam.getSourceType());
            inventoryUnApproveDTO.setBillId(billId);
            this.unApprove(inventoryUnApproveDTO);
        });
    }

}