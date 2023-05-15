package com.erp.server.wms.service.impl;

import com.common.core.utils.ValidatorUtil;
import com.erp.model.wms.dto.inventory.*;
import com.erp.model.wms.enums.inventory.InventoryBizTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.server.wms.config.InventoryHelper;
import com.erp.server.wms.service.InventoryTransCoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @Classname: InventoryTransCoreServiceImpl
 * @Description: 库存交易核心处理类
 * @CreateTime: 2023-05-06  09:30
 * @Author: zhangchunlin
 */
@Service
public class InventoryTransCoreServiceImpl implements InventoryTransCoreService {


    @Resource
    private InventoryHelper inventoryHelper;

    /**
     * 出入库业务，按业务类型（走配置的交易规则）
     * @param dto
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approveByType(InventoryInOutStockDTO dto) {
        ValidatorUtil.validateEntity(dto);
        AbstractInventoryServiceImpl abstractInventoryService = inventoryHelper.getInventoryService(InventoryBizTypeEnum.IN_OUT_STOCK);
        abstractInventoryService.approve(dto.getMembers(), null, InventoryBusinessTypeEnum.of(dto.getBusinessType()), true);
    }

    /**
     * 调拨业务，按业务类型（走配置的交易规则）
     * @param dto
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approveByType(InventoryTransferDTO dto) {
        ValidatorUtil.validateEntity(dto);
        AbstractInventoryServiceImpl abstractInventoryService = inventoryHelper.getInventoryService(InventoryBizTypeEnum.TRANSFER_STOCK);
        abstractInventoryService.approve(dto.getMembers(), null, InventoryBusinessTypeEnum.of(dto.getBusinessType()), true);
    }

    /**
     * 调拨业务，自定义规则
     * @param dto
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approveByRule(InventoryTransferRuleDTO dto) {
        ValidatorUtil.validateEntity(dto);
        AbstractInventoryServiceImpl abstractInventoryService = inventoryHelper.getInventoryService(InventoryBizTypeEnum.TRANSFER_STOCK);
        abstractInventoryService.approve(dto.getMembers(), dto.getRules(), InventoryBusinessTypeEnum.of(dto.getBusinessType()), false);
    }

    /**
     * 反审核
     * @param dto
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unApprove(InventoryUnApproveDTO dto) {
        ValidatorUtil.validateEntity(dto);
        inventoryHelper.getInventoryService(InventoryBizTypeEnum.IN_OUT_STOCK).unApprove(dto);
    }

    /**
     * 批量反审核
     * @param dto
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchUnApprove(InventoryBatchUnApproveDTO dto) {
        ValidatorUtil.validateEntity(dto);
        dto.getBillIds().stream().forEach(billId->{
            InventoryUnApproveDTO inventoryUnApproveDTO = new InventoryUnApproveDTO();
            inventoryUnApproveDTO.setSourceType(dto.getSourceType());
            inventoryUnApproveDTO.setBillId(billId);
            inventoryHelper.getInventoryService(InventoryBizTypeEnum.IN_OUT_STOCK).unApprove(inventoryUnApproveDTO);
        });
    }

}