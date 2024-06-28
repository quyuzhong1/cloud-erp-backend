package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryUnApproveDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.enums.inventory.InventoryBizTypeEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
import com.erp.server.wms.config.VirtualInventoryHelper;
import com.erp.server.wms.service.VirtualInventoryStockService;
import com.erp.server.wms.service.VirtualInventoryTransCoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 虚拟库存交易核心处理类
 * @author will
 * @date 2024/6/5 17:44
 */
@Service
public class VirtualInventoryTransCoreServiceImpl implements VirtualInventoryTransCoreService {


    @Resource
    private VirtualInventoryHelper virtualInventoryHelper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approve(VirtualInventoryStockDTO.StockParamDTO dto) {
        ValidatorUtil.validateEntity(dto);
        VirtualInventoryStockService virtualInventoryStockService = virtualInventoryHelper.getInventoryService(InventoryBizTypeEnum.IN_OUT_STOCK);
        virtualInventoryStockService.approve(dto.getParamList(), dto.getRules(), VirtualInventoryBusinessTypeEnum.getByCode(dto.getBusinessType()), CollectionUtil.isEmpty(dto.getRules()) ? true : false);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approve(VirtualInventoryStockDTO.TransferParamDTO dto) {
        ValidatorUtil.validateEntity(dto);
        VirtualInventoryStockService virtualInventoryStockService = virtualInventoryHelper.getInventoryService(InventoryBizTypeEnum.TRANSFER_STOCK);
        virtualInventoryStockService.approve(dto.getParamList(), dto.getRules(), VirtualInventoryBusinessTypeEnum.getByCode(dto.getBusinessType()), CollectionUtil.isEmpty(dto.getRules()) ? true : false);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unApprove(InventoryUnApproveDTO dto) {
        ValidatorUtil.validateEntity(dto);
        virtualInventoryHelper.getInventoryService(InventoryBizTypeEnum.IN_OUT_STOCK)
                .unApprove(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchUnApprove(InventoryBatchUnApproveDTO dto) {
        ValidatorUtil.validateEntity(dto);
        VirtualInventoryStockService virtualInventoryStockService = virtualInventoryHelper.getInventoryService(InventoryBizTypeEnum.IN_OUT_STOCK);
        dto.getBillIds().forEach(billId->{
            InventoryUnApproveDTO inventoryUnApproveDTO = new InventoryUnApproveDTO();
            inventoryUnApproveDTO.setSourceType(dto.getSourceType());
            inventoryUnApproveDTO.setBillId(billId);
            virtualInventoryStockService.unApprove(inventoryUnApproveDTO);
        });
    }

}