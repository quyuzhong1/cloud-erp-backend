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
@Service
public class InventoryTransCoreServiceImpl implements InventoryTransCoreService {


    @Resource
    private InventoryHelper inventoryHelper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approveByType(InventoryInOutStockDTO dto) {
        ValidatorUtil.validateEntity(dto);
        InventoryStockService inventoryService = inventoryHelper.getInventoryService(InventoryBizTypeEnum.IN_OUT_STOCK);
        inventoryService.approve(dto.getParamList(), null, InventoryBusinessTypeEnum.getByCode(dto.getBusinessType()), true);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approveByType(InventoryTransferDTO dto) {
        ValidatorUtil.validateEntity(dto);
        InventoryStockService inventoryService = inventoryHelper.getInventoryService(InventoryBizTypeEnum.TRANSFER_STOCK);
        inventoryService.approve(dto.getParamList(), null, InventoryBusinessTypeEnum.getByCode(dto.getBusinessType()), true);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approveByRule(InventoryTransferRuleDTO dto) {
        ValidatorUtil.validateEntity(dto);
        InventoryStockService inventoryService = inventoryHelper.getInventoryService(InventoryBizTypeEnum.TRANSFER_STOCK);
        inventoryService.approve(dto.getParamList(), dto.getRules(), InventoryBusinessTypeEnum.getByCode(dto.getBusinessType()), false);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approveByRule(InventoryInOutStockRuleDTO dto) {
        ValidatorUtil.validateEntity(dto, ValidGroup.Update.class);
        InventoryStockService inventoryService = inventoryHelper.getInventoryService(InventoryBizTypeEnum.IN_OUT_STOCK);
        inventoryService.approve(dto.getParamList(), dto.getRules(), InventoryBusinessTypeEnum.getByCode(dto.getBusinessType()), false);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unApprove(InventoryUnApproveDTO dto) {
        ValidatorUtil.validateEntity(dto);
        inventoryHelper.getInventoryService(InventoryBizTypeEnum.IN_OUT_STOCK)
                .unApprove(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchUnApprove(InventoryBatchUnApproveDTO dto) {
        ValidatorUtil.validateEntity(dto);
        InventoryStockService inventoryService = inventoryHelper.getInventoryService(InventoryBizTypeEnum.IN_OUT_STOCK);
        dto.getBillIds().forEach(billId->{
            InventoryUnApproveDTO inventoryUnApproveDTO = new InventoryUnApproveDTO();
            inventoryUnApproveDTO.setSourceType(dto.getSourceType());
            inventoryUnApproveDTO.setBillId(billId);
            inventoryService.unApprove(inventoryUnApproveDTO);
        });
    }

}