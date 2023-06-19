package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.server.wms.mapper.PurchaseOrderMapper;
import com.erp.server.wms.service.PurchaseOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PurchaseOrderServiceImpl extends SuperServiceImpl<PurchaseOrderMapper, PurchaseOrderEntity> implements PurchaseOrderService {

    @Override
    public Boolean saveOrUpdatePurchaseOrder(List<PurchaseOrderEntity> purchaseOrderEntityList) {
        List<String> detailIds = purchaseOrderEntityList.stream().map(PurchaseOrderEntity::getId).collect(Collectors.toList());
        List<PurchaseOrderEntity> detailEntityList = this.listByIds(detailIds);
        List<String> ids = purchaseOrderEntityList.stream().map(PurchaseOrderEntity::getId).collect(Collectors.toList());
        List<String> dbIds = detailEntityList.stream().map(PurchaseOrderEntity::getId).collect(Collectors.toList());
        List<String> existIdList = ids.stream().filter(s -> dbIds.contains(s)).collect(Collectors.toList());
        List<String> notExistIdList = ids.stream().filter(s -> !dbIds.contains(s)).collect(Collectors.toList());
        List<PurchaseOrderEntity> existDetailEntityList = this.listByIds(existIdList);
        List<PurchaseOrderEntity> notExistDetailEntityList = this.listByIds(notExistIdList);
        if (CollectionUtils.isNotEmpty(existDetailEntityList)) {
            this.updateBatchById(existDetailEntityList);
        }
        if (CollectionUtils.isNotEmpty(notExistDetailEntityList)) {
            this.saveBatch(notExistDetailEntityList);
        }
        return Boolean.TRUE;
    }
}
