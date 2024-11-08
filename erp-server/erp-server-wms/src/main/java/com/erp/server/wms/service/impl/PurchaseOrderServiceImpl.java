package com.erp.server.wms.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.server.wms.mapper.PurchaseOrderMapper;
import com.erp.server.wms.service.PurchaseOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PurchaseOrderServiceImpl extends SuperServiceImpl<PurchaseOrderMapper, PurchaseOrderEntity> implements PurchaseOrderService {

    @Override
    public List<PurchaseOrderEntity> ListPurchaseOrderEntityByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return listByIds(ids);
    }

    @Override
    public Boolean saveOrUpdatePurchaseOrder(List<PurchaseOrderEntity> purchaseOrderEntityList) {
        List<String> detailIds = purchaseOrderEntityList.stream().map(PurchaseOrderEntity::getId).collect(Collectors.toList());
        List<PurchaseOrderEntity> detailEntityList = this.listByIds(detailIds);
        List<String> ids = purchaseOrderEntityList.stream().map(PurchaseOrderEntity::getId).collect(Collectors.toList());
        List<String> dbIds = detailEntityList.stream().map(PurchaseOrderEntity::getId).collect(Collectors.toList());
        List<String> existIdList = ids.stream().filter(s -> dbIds.contains(s)).collect(Collectors.toList());
        List<String> notExistIdList = detailIds.stream().filter(s -> !dbIds.contains(s)).collect(Collectors.toList());
        List<PurchaseOrderEntity> notExistDetailEntityList = new ArrayList<>();
        List<PurchaseOrderEntity> existDetailEntityList = new ArrayList<>();
        for (PurchaseOrderEntity detailEntity : purchaseOrderEntityList) {
            if (notExistIdList.contains(detailEntity.getId())) {
                notExistDetailEntityList.add(detailEntity);
            }
            if (existIdList.contains(detailEntity.getId())) {
                existDetailEntityList.add(detailEntity);
            }
        }
        if (CollectionUtils.isNotEmpty(notExistDetailEntityList)) {
            this.saveBatch(notExistDetailEntityList);
        }
        if (CollectionUtils.isNotEmpty(existDetailEntityList)) {
            baseMapper.updateBatchSelective(existDetailEntityList);
        }
        return Boolean.TRUE;
    }
}
