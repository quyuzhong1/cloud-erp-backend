package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.server.wms.mapper.PurchaseOrderMapper;
import com.erp.server.wms.mapper.PurchaseOrderSupplierMapper;
import com.erp.server.wms.service.PurchaseOrderSupplierService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PurchaseOrderSupplierServiceImpl extends SuperServiceImpl<PurchaseOrderSupplierMapper, PurchaseOrderSupplierEntity> implements PurchaseOrderSupplierService {

    @Override
    public List<PurchaseOrderSupplierEntity> ListPurchaseOrderSupplierEntityByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return baseMapper.listByIds(ids);
    }

    @Override
    public Boolean saveOrUpdatePurchaseOrderSupplier(List<PurchaseOrderSupplierEntity> purchaseOrderSupplierEntityList) {
        List<String> detailIds = purchaseOrderSupplierEntityList.stream().map(PurchaseOrderSupplierEntity::getId).collect(Collectors.toList());
        List<PurchaseOrderSupplierEntity> detailEntityList = ListPurchaseOrderSupplierEntityByIds(detailIds);
        List<String> ids = purchaseOrderSupplierEntityList.stream().map(PurchaseOrderSupplierEntity::getId).collect(Collectors.toList());
        List<String> dbIds = detailEntityList.stream().map(PurchaseOrderSupplierEntity::getId).collect(Collectors.toList());
        List<String> existIdList = ids.stream().filter(s -> dbIds.contains(s)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(existIdList)) {
            List<PurchaseOrderSupplierEntity> existDetailEntityList = ListPurchaseOrderSupplierEntityByIds(existIdList);
            if (CollectionUtils.isNotEmpty(existDetailEntityList)) {
                baseMapper.updateBatchSelective(existDetailEntityList);
            }
        }
        List<String> notExistIdList = detailIds.stream().filter(s -> !dbIds.contains(s)).collect(Collectors.toList());
        List<PurchaseOrderSupplierEntity> notExistDetailEntityList = new ArrayList<>();
        for (PurchaseOrderSupplierEntity detailEntity : purchaseOrderSupplierEntityList) {
            if (notExistIdList.contains(detailEntity.getId())) {
                notExistDetailEntityList.add(detailEntity);
            }
        }
        if (CollectionUtils.isNotEmpty(notExistDetailEntityList)) {
            this.saveBatch(notExistDetailEntityList);
        }
        return Boolean.TRUE;
    }
}
