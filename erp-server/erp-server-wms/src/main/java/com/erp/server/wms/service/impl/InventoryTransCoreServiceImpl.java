package com.erp.server.wms.service.impl;

import com.common.core.utils.ValidatorUtil;
import com.erp.model.wms.dto.inventory.InventoryInStockOrOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryTransferDTO;
import com.erp.model.wms.dto.inventory.InventoryTransferRuleDTO;
import com.erp.model.wms.dto.inventory.InventoryUnApproveDTO;
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
    public void approveInOutStockByType(InventoryInStockOrOutStockDTO dto) {
        ValidatorUtil.validateEntity(dto);
        inventoryHelper.getInventoryService(InventoryBizTypeEnum.IN_OUT_STOCK).approve(dto.getSkus(), null, InventoryBusinessTypeEnum.of(dto.getBusinessType()), true);
    }

    /**
     * 调拨业务，按业务类型（走配置的交易规则）
     * @param dto
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approveTransferByType(InventoryTransferDTO dto) {
        ValidatorUtil.validateEntity(dto);
        inventoryHelper.getInventoryService(InventoryBizTypeEnum.TRANSFER_STOCK).approve(dto.getSkus(), null, InventoryBusinessTypeEnum.of(dto.getBusinessType()), true);
    }

    /**
     * 调拨业务，自定义规则
     * @param dto
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approveByRule(InventoryTransferRuleDTO dto) {
        ValidatorUtil.validateEntity(dto);
        inventoryHelper.getInventoryService(InventoryBizTypeEnum.TRANSFER_STOCK).approve(dto.getSkus(), dto.getRules(), InventoryBusinessTypeEnum.of(dto.getBusinessType()), false);
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

}