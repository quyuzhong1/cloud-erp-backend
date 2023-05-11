package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.TransactionFlowDTO;
import com.erp.model.wms.entity.TransactionFlowEntity;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryModeEnum;
import com.erp.model.wms.enums.inventory.InventoryOperationModeEnum;
import com.erp.server.wms.mapper.TransactionFlowMapper;
import com.erp.server.wms.service.CommonService;
import com.erp.server.wms.service.TransactionFlowService;
import com.erp.server.wms.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Classname: TransactionFlowServiceImpl
 * @Description: TODO
 * @CreateTime: 2023-04-25  19:43
 * @Author: zhangchunlin
 */
@Service
public class TransactionFlowServiceImpl extends SuperServiceImpl<TransactionFlowMapper, TransactionFlowEntity> implements TransactionFlowService {

    @Autowired
    private TransactionFlowMapper transactionFlowMapper;

    @Autowired
    private CommonService commonService;

    @Autowired
    private WarehouseService warehouseService;

    @Override
    public List<TransactionFlowEntity> getUnApprovedTxnFlows(String sourceType, String sourceId) {
        List<TransactionFlowEntity> txnFlows =  lambdaQuery().eq(TransactionFlowEntity::getSourceType, sourceType)
                .eq(TransactionFlowEntity::getSourceId, sourceId).eq(TransactionFlowEntity::getOperationMode, InventoryOperationModeEnum.APPROVE.getCode())
                .eq(TransactionFlowEntity::getIsUnapproved, Boolean.FALSE).list();
        if(CollUtil.isNotEmpty(txnFlows)) {
            txnFlows = txnFlows.stream().sorted(Comparator.comparing(TransactionFlowEntity::getCreateTime)).collect(Collectors.toList());
        }
        return txnFlows;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateUnapprovedById(String id, Integer version) {
        LoginUser loginUser =  commonService.getUserInfo();
        return transactionFlowMapper.updateUnapprovedById(id, version, LocalDateTime.now(), loginUser.getUid(), loginUser.getUserName());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(TransactionFlowDTO param, InventoryBusinessTypeEnum businessType, String transactionRuleId, Integer afterInventoryQty, InventoryModeEnum inventoryModeEnum) {
        // 记录交易流水
        TransactionFlowEntity transactionFlowEntity = new TransactionFlowEntity();
        transactionFlowEntity.setBillDate(param.getBillDate());
        transactionFlowEntity.setInventoryId(param.getInventoryId());
        transactionFlowEntity.setInventoryDetailId(param.getInventoryDetailId());
        transactionFlowEntity.setOrgId(param.getOrgId());
        // 获取仓库名称
        WarehouseDTO.UpdateDTO warehouse = warehouseService.detailWithCache(param.getWarehouseId());
        ValidatorUtil.isTrue(Objects.nonNull(warehouse) && StrUtils.isNotEmpty(warehouse.getId()),()->new ServiceException(ApiError.ERROR_99002));

        transactionFlowEntity.setWarehouseId(param.getWarehouseId());
        transactionFlowEntity.setWarehouseName(warehouse.getName());
        transactionFlowEntity.setWarehouseLocation(param.getWarehouseLocation());
        // TODO 暂时还没有库位表
        transactionFlowEntity.setDictInventoryStatus(param.getDictInventoryStatus());
        // 批次日期取库存明细表上关联的日期
        transactionFlowEntity.setInstockBatchDate(param.getInstockBatchDate());
        transactionFlowEntity.setSkuId(param.getSkuId());
        transactionFlowEntity.setSkuNo(param.getSkuNo());
        transactionFlowEntity.setSourceType(param.getSourceType());
        transactionFlowEntity.setSourceId(param.getSourceId());
        transactionFlowEntity.setSourceCode(param.getSourceCode());
        transactionFlowEntity.setSourceDetailId(param.getSourceDetailId());
        transactionFlowEntity.setDictBizType(businessType.getCode());
        LoginUser loginUser = commonService.getUserInfo();
        transactionFlowEntity.setUserId(Objects.nonNull(loginUser) ? loginUser.getUid() : "");
        transactionFlowEntity.setTradeTime(LocalDateTime.now());
        transactionFlowEntity.setTransactionRuleId(StrUtils.null2EmptyWithTrim(transactionRuleId));
        Integer qty = param.getQty();
        if(Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) {
            qty = qty * -1;
        }
        transactionFlowEntity.setQty(qty);
        transactionFlowEntity.setCurInventoryQty(afterInventoryQty);
        transactionFlowEntity.setOperationMode(StrUtils.null2EmptyWithTrim(param.getOperationMode()));
        transactionFlowEntity.setVersion(1);
        transactionFlowEntity.setTransactionNo(param.getTransactionNo());
        super.save(transactionFlowEntity);
    }

}